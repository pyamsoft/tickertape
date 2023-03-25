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
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.pydroid.util.ifNotCancellation
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
import com.pyamsoft.tickertape.ui.ListGenerateResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
) : DeleteRestoreViewModeler<PortfolioViewState>(state) {

  private var internalFullPortfolio: List<PortfolioStock> = emptyList()

  private val portfolioFetcher =
      highlander<ResultWrapper<List<PortfolioStock>>, Boolean> { force ->
        if (force) {
          interactorCache.invalidatePortfolio()
        }
        return@highlander interactor.getPortfolio()
      }

  private val portfolioGenerator =
      highlander<PortfolioListGenerateResult, PortfolioViewState, List<PortfolioStock>> {
          state,
          tickers ->
        val full = tickers.sortedWith(PortfolioStock.COMPARATOR)
        val portfolio = PortfolioStockList.of(full)
        val stocks = state.asVisible(full)
        return@highlander PortfolioListGenerateResult(
            all = full,
            portfolio = portfolio,
            visible = stocks,
        )
      }

  private fun CoroutineScope.handleSplitRealtimeEvent(event: SplitChangeEvent) {
    Timber.d("A split change has happened, re-process the entire list: $event")
    handleRefreshList(this, false)
  }

  private fun CoroutineScope.handlePositionRealtimeEvent(event: PositionChangeEvent) {
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

  private fun CoroutineScope.handleUpdatePosition(position: DbPosition) {
    val s = state
    s.regeneratePortfolio(this) {
      internalFullPortfolio.map { stock ->
        return@map if (stock.holding.id != position.holdingId) stock
        else {
          val newPositions =
              stock.positions.map { if (doesPositionMatch(it, position)) position else it }
          stock.copy(positions = newPositions)
        }
      }
    }
  }

  private fun CoroutineScope.handleInsertPosition(position: DbPosition) {
    val s = state
    s.regeneratePortfolio(this) {
      internalFullPortfolio.map { stock ->
        return@map if (stock.holding.id != position.holdingId) stock
        else stock.copy(positions = stock.positions + position)
      }
    }
  }

  private fun CoroutineScope.handleDeletePosition(position: DbPosition) {
    // On delete, we don't need to re-fetch quotes from the network

    val s = state
    s.regeneratePortfolio(this) {
      internalFullPortfolio.map { stock ->
        if (stock.holding.id != position.holdingId) {
          return@map stock
        } else {
          return@map if (!stock.positions.contains { doesPositionMatch(it, position) }) stock
          else {
            stock.copy(positions = stock.positions.filterNot { doesPositionMatch(it, position) })
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

  private inline fun MutablePortfolioViewState.regeneratePortfolio(
      scope: CoroutineScope,
      crossinline stocks: () -> List<PortfolioStock>
  ) {
    val self = this

    // Default dispatcher for performance
    scope.launch(context = Dispatchers.Default) {
      // Cancel any old processing
      try {
        val result = portfolioGenerator.call(state, stocks())
        internalFullPortfolio = result.all
        self.portfolio.value = result.portfolio
        self.stocks.value = result.visible
      } catch (e: Throwable) {
        e.ifNotCancellation {
          Timber.e(e, "Error occurred while regenerating list")

          // Clear data on bad processing
          internalFullPortfolio = emptyList()
          self.portfolio.value = PortfolioStockList.empty()
          self.stocks.value = emptyList()
        }
      }
    }
  }

  @CheckResult
  private fun PortfolioViewState.asVisible(tickers: List<PortfolioStock>): List<PortfolioStock> {
    val search = this.query.value
    val section = this.section.value
    return tickers
        .asSequence()
        .filter { ps ->
          val symbol = ps.holding.symbol.raw
          val name = ps.ticker?.quote?.company?.company
          return@filter if (symbol.contains(search, ignoreCase = true)) true
          else name?.contains(search, ignoreCase = true) ?: false
        }
        .filter { ps ->
          val type = ps.holding.type
          return@filter when (section) {
            EquityType.STOCK -> type == EquityType.STOCK
            EquityType.OPTION -> type == EquityType.OPTION
            EquityType.CRYPTOCURRENCY -> type == EquityType.CRYPTOCURRENCY
          }
        }
        .toList()
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

  private fun CoroutineScope.handleDeleteHolding(holding: DbHolding, offerUndo: Boolean) {
    // On delete, we don't need to re-fetch quotes from the network
    val s = state

    s.regeneratePortfolio(this) { internalFullPortfolio.filterNot { it.holding.id == holding.id } }

    if (offerUndo) {
      Timber.d("Offer undo on holding delete: $holding")
      s.recentlyDeleteHolding.value = holding
    }
  }

  private fun handleOpenDelete(params: PortfolioRemoveParams) {
    state.remove.value = params
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
  }

  fun handleRefreshList(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == PortfolioViewState.LoadingState.LOADING) {
      return
    }

    state.loadingState.value = PortfolioViewState.LoadingState.LOADING
    scope.launch(context = Dispatchers.Main) {
      portfolioFetcher
          .call(force)
          .onSuccess { list ->
            state.apply {
              regeneratePortfolio(scope) { list }
              error.value = null
            }
          }
          .onFailure { Timber.e(it, "Failed to refresh entry list") }
          .onFailure {
            state.apply {
              regeneratePortfolio(scope) { emptyList() }
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

  fun handleRegenerateList(scope: CoroutineScope) {
    val s = state
    s.regeneratePortfolio(scope) { internalFullPortfolio }
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

  fun handleHoldingDeleteFinal(scope: CoroutineScope) {
    handleDeleteFinal(state.recentlyDeleteHolding) {
      scope.handleDeleteHolding(it, offerUndo = false)
    }
  }

  fun handleRestoreDeletedHolding(scope: CoroutineScope) {
    handleRestoreDeleted(
        scope = scope,
        recentlyDeleted = state.recentlyDeleteHolding,
    ) {
      interactor.restoreHolding(it)
    }
  }

  private data class PortfolioListGenerateResult(
      val portfolio: PortfolioStockList,
      override val all: List<PortfolioStock>,
      override val visible: List<PortfolioStock>,
  ) : ListGenerateResult<PortfolioStock>

  companion object {
    private const val KEY_SEARCH = "key_portfolio_search"
    private const val KEY_REMOVE = "key_portfolio_remove_dialog"
  }
}
