/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.main.MainSelectionEvent
import com.pyamsoft.tickertape.quote.DeleteRestoreViewModeler
import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PortfolioViewModeler
@Inject
internal constructor(
    override val state: MutablePortfolioViewState,
    private val interactor: PortfolioInteractor,
    private val interactorCache: PortfolioInteractor.Cache,
    private val mainSelectionConsumer: EventConsumer<MainSelectionEvent>,
    private val jsonParser: JsonParser,
    private val processor: PortfolioProcessor,
) : DeleteRestoreViewModeler<PortfolioViewState>(state) {

  private var fullPortfolio = MutableStateFlow(emptyList<PortfolioStock>())

  @CheckResult
  private suspend fun fetchPortfolio(force: Boolean): ResultWrapper<List<PortfolioStock>> {
    if (force) {
      interactorCache.invalidatePortfolio()
    }
    return interactor.getPortfolio()
  }

  private fun CoroutineScope.handleSplitRealtimeEvent(event: SplitChangeEvent) {
    Timber.d("A split change has happened, re-process the entire list: $event")
    handleRefreshList(this, false)
  }

  private fun handlePositionRealtimeEvent(event: PositionChangeEvent) {
    return when (event) {
      is PositionChangeEvent.Delete -> handleDeletePosition(event.position)
      is PositionChangeEvent.Insert -> handleInsertPosition(event.position)
      is PositionChangeEvent.Update -> handleUpdatePosition(event.position)
    }
  }

  @CheckResult
  private fun doesPositionMatch(p1: DbPosition, p2: DbPosition): Boolean {
    return p1.id == p2.id
  }

  private fun handleUpdatePosition(position: DbPosition) {
    fullPortfolio.update { p ->
      p.map { stock ->
        return@map if (stock.holding.id != position.holdingId) stock
        else {
          val newPositions =
              stock.positions.map { if (doesPositionMatch(it, position)) position else it }
          stock.copy(positions = newPositions)
        }
      }
    }
  }

  private fun handleInsertPosition(position: DbPosition) {
    fullPortfolio.update { p ->
      p.map { stock ->
        return@map if (stock.holding.id != position.holdingId) stock
        else stock.copy(positions = stock.positions + position)
      }
    }
  }

  private fun handleDeletePosition(position: DbPosition) {
    // On delete, we don't need to re-fetch quotes from the network
    fullPortfolio.update { p ->
      p.map { stock ->
        if (stock.holding.id != position.holdingId) {
          return@map stock
        } else {
          return@map if (!stock.positions.contains { doesPositionMatch(it, position) }) stock
          else {
            stock.copy(
                positions = stock.positions.filterNot { doesPositionMatch(it, position) },
            )
          }
        }
      }
    }
  }

  private fun CoroutineScope.handleHoldingRealtimeEvent(event: HoldingChangeEvent) {
    return when (event) {
      is HoldingChangeEvent.Delete -> handleDeleteHolding(event.holding, event.offerUndo)
      is HoldingChangeEvent.Insert -> handleInsertHolding(event.holding)
      is HoldingChangeEvent.Update -> handleUpdateHolding(event.holding)
    }
  }

  private fun CoroutineScope.handleUpdateHolding(holding: DbHolding) {
    Timber.d("Holding updated: $holding")

    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    handleRefreshList(scope = this, force = true)
  }

  private fun CoroutineScope.handleInsertHolding(holding: DbHolding) {
    Timber.d("Holding inserted: $holding")

    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    handleRefreshList(scope = this, force = true)
  }

  private fun handleDeleteHolding(holding: DbHolding, offerUndo: Boolean) {
    // On delete, we don't need to re-fetch quotes from the network
    val s = state

    fullPortfolio.update { p -> p.filterNot { it.holding.id == holding.id } }

    if (offerUndo) {
      Timber.d("Offer undo on holding delete: $holding")
      s.recentlyDeleteHolding.value = holding
    }
  }

  private fun handleOpenDelete(params: PortfolioRemoveParams) {
    state.remove.value = params
  }

  private fun generateList(
      scope: CoroutineScope,
  ) {
    // Create a source that generates data based on the latest from all sources
    val combined =
        combineTransform(
            fullPortfolio,
            state.query,
            state.section,
        ) { all, search, section ->
          emit(
              ItemPayload(
                  stocks = all,
                  search = search,
                  section = section,
              ),
          )
        }

    scope.launch(context = Dispatchers.Default) {
      combined.collect { (stocks, search, section) ->
        state.portfolio.value = processor.process(stocks)
        state.stocks.value =
            stocks
                .asSequence()
                .run {
                  if (search.isBlank()) {
                    this
                  } else {
                    filter { ps ->
                      val symbol = ps.holding.symbol.raw
                      val name = ps.ticker?.quote?.company?.company
                      return@filter if (symbol.contains(search, ignoreCase = true)) true
                      else name?.contains(search, ignoreCase = true) ?: false
                    }
                  }
                }
                .filter { ps ->
                  val type = ps.holding.type
                  return@filter when (section) {
                    EquityType.STOCK -> type == EquityType.STOCK
                    EquityType.OPTION -> type == EquityType.OPTION
                    EquityType.CRYPTOCURRENCY -> type == EquityType.CRYPTOCURRENCY
                  }
                }
                .sortedWith(PortfolioStock.COMPARATOR)
                .toList()
      }
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_SEARCH) { s.query.value }.also { add(it) }

        registry
            .registerProvider(KEY_REMOVE) { s.remove.value?.let { jsonParser.toJson(it.toJson()) } }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry.consumeRestored(KEY_SEARCH)?.let { it as String }?.also { s.query.value = it }

    registry
        .consumeRestored(KEY_REMOVE)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<PortfolioRemoveParams.Json>(it) }
        ?.fromJson()
        ?.also { handleOpenDelete(it) }
  }

  fun bind(
      scope: CoroutineScope,
      onMainSelectionEvent: () -> Unit,
  ) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForHoldingChanges { handleHoldingRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      interactor.listenForPositionChanges { handlePositionRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      interactor.listenForSplitChanges { handleSplitRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      mainSelectionConsumer.onEvent { event ->
        if (event.page == MainPage.Portfolio) {
          onMainSelectionEvent()
        }
      }
    }

    generateList(scope = scope)
  }

  fun handleRefreshList(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == PortfolioViewState.LoadingState.LOADING) {
      return
    }

    scope.launch(context = Dispatchers.Main) {
      if (state.loadingState.value == PortfolioViewState.LoadingState.LOADING) {
        return@launch
      }

      state.loadingState.value = PortfolioViewState.LoadingState.LOADING
      fetchPortfolio(force)
          .onSuccess { list ->
            state.apply {
              fullPortfolio.value = list
              error.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to refresh entry list") }
          .onFailure {
            state.apply {
              fullPortfolio.value = emptyList()
              error.value = it
            }
          }
          .onFinally { state.loadingState.value = PortfolioViewState.LoadingState.DONE }
    }
  }

  fun handleSearch(query: String) {
    state.query.value = query
  }

  fun handleSectionChanged(tab: EquityType) {
    state.section.value = tab
  }

  fun handleOpenDelete(stock: PortfolioStock) {
    handleOpenDelete(
        PortfolioRemoveParams(
            symbol = stock.holding.symbol,
            holdingId = stock.holding.id,
        ),
    )
  }

  fun handleCloseDelete() {
    state.remove.value = null
  }

  fun handleHoldingDeleteFinal() {
    handleDeleteFinal(state.recentlyDeleteHolding) { handleDeleteHolding(it, offerUndo = false) }
  }

  fun handleRestoreDeletedHolding(scope: CoroutineScope) {
    handleRestoreDeleted(
        scope = scope,
        recentlyDeleted = state.recentlyDeleteHolding,
    ) {
      interactor.restoreHolding(it)
    }
  }

  private data class ItemPayload(
      val stocks: List<PortfolioStock>,
      val search: String,
      val section: EquityType,
  )

  companion object {
    private const val KEY_SEARCH = "key_portfolio_search"
    private const val KEY_REMOVE = "key_portfolio_remove_dialog"
  }
}
