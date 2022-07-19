/*
 * Copyright 2021 Peter Kenji Yamanaka
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
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.ListGenerateResult
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PortfolioViewModeler
@Inject
internal constructor(
    private val state: MutablePortfolioViewState,
    private val interactor: PortfolioInteractor,
) : AbstractViewModeler<PortfolioViewState>(state) {

  private val portfolioFetcher =
      highlander<ResultWrapper<List<PortfolioStock>>, Boolean> { interactor.getPortfolio(it) }

  private val searchRunner =
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
    Timber.d("A split change has happened, re-process the entire list")
    handleRefreshList(this, false)
  }

  private fun CoroutineScope.handlePositionRealtimeEvent(event: PositionChangeEvent) {
    return when (event) {
      is PositionChangeEvent.Delete -> handleDeletePosition(event.position, event.offerUndo)
      is PositionChangeEvent.Insert -> handleInsertPosition(event.position)
      is PositionChangeEvent.Update -> handleUpdatePosition(event.position)
    }
  }

  private fun CoroutineScope.handleUpdatePosition(position: DbPosition) {
    val doesPositionMatch = { p: DbPosition -> p.id() == position.id() }
    val s = state
    s.regeneratePortfolio(this) {
      s.fullPortfolio.map { stock ->
        return@map if (stock.holding.id() != position.holdingId()) stock
        else {
          val newPositions = stock.positions.map { if (doesPositionMatch(it)) position else it }
          stock.copy(positions = newPositions)
        }
      }
    }
  }

  private fun CoroutineScope.handleInsertPosition(position: DbPosition) {
    val s = state
    s.regeneratePortfolio(this) {
      s.fullPortfolio.map { stock ->
        return@map if (stock.holding.id() != position.holdingId()) stock
        else stock.copy(positions = stock.positions + position)
      }
    }
  }

  private fun CoroutineScope.handleDeletePosition(position: DbPosition, offerUndo: Boolean) {
    val doesPositionMatch = { p: DbPosition -> p.id() == position.id() }
    val s = state
    s.regeneratePortfolio(this) {
      s.fullPortfolio.map { stock ->
        if (stock.holding.id() != position.holdingId()) {
          return@map stock
        } else {
          return@map if (!stock.positions.contains(doesPositionMatch)) stock
          else {
            stock.copy(positions = stock.positions.filterNot(doesPositionMatch))
          }
        }
      }
    }
    // TODO offer up undo ability

    // On delete, we don't need to re-fetch quotes from the network
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
        val result = searchRunner.call(state, stocks())
        self.fullPortfolio = result.all
        self.portfolio = result.portfolio
        self.stocks = result.visible
      } catch (e: Throwable) {
        e.ifNotCancellation {
          Timber.e(e, "Error occurred while regenerating list")

          // Clear data on bad processing
          self.fullPortfolio = emptyList()
          self.portfolio = PortfolioStockList.empty()
          self.stocks = emptyList()
        }
      }
    }
  }

  @CheckResult
  private fun PortfolioViewState.asVisible(tickers: List<PortfolioStock>): List<PortfolioStock> {
    val search = this.query
    val section = this.section
    return tickers
        .asSequence()
        .filter { ps ->
          val symbol = ps.holding.symbol().raw
          val name = ps.ticker?.quote?.company()?.company()
          return@filter if (symbol.contains(search, ignoreCase = true)) true
          else name?.contains(search, ignoreCase = true) ?: false
        }
        .filter { ps ->
          val type = ps.holding.type()
          return@filter when (section) {
            EquityType.STOCK -> type == EquityType.STOCK
            EquityType.OPTION -> type == EquityType.OPTION
            EquityType.CRYPTOCURRENCY -> type == EquityType.CRYPTOCURRENCY
          }
        }
        .toList()
  }

  private fun CoroutineScope.handleUpdateHolding(holding: DbHolding) {
    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    handleRefreshList(scope = this, force = true)
  }

  private fun CoroutineScope.handleInsertHolding(holding: DbHolding) {
    // Don't actually insert anything to the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    handleRefreshList(scope = this, force = true)
  }

  private fun CoroutineScope.handleDeleteHolding(holding: DbHolding, offerUndo: Boolean) {
    val s = state
    s.regeneratePortfolio(this) { s.fullPortfolio.filterNot { it.holding.id() == holding.id() } }
    // TODO offer up undo ability

    // On delete, we don't need to re-fetch quotes from the network
  }

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    savedInstanceState.get<String>(KEY_SEARCH)?.also { state.query = it }
  }

  override fun saveState(outState: UiSavedStateWriter) {
    state.query.also { search ->
      if (search.isBlank()) {
        outState.remove(KEY_SEARCH)
      } else {
        outState.put(KEY_SEARCH, search.trim())
      }
    }
  }

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForHoldingChanges { handleHoldingRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      interactor.listenForPositionChanges { handlePositionRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      interactor.listenForSplitChanges { handleSplitRealtimeEvent(it) }
    }
  }

  fun handleRefreshList(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      portfolioFetcher
          .call(force)
          .onSuccess { list ->
            state.apply {
              regeneratePortfolio(scope) { list }
              error = null
            }
          }
          .onFailure { Timber.e(it, "Failed to refresh entry list") }
          .onFailure {
            state.apply {
              regeneratePortfolio(scope) { emptyList() }
              error = it
            }
          }
          .onFinally { state.isLoading = false }
    }
  }

  fun handleSearch(query: String) {
    state.query = query
  }

  fun handleSectionChanged(tab: EquityType) {
    state.section = tab
  }

  fun handleRegenerateList(scope: CoroutineScope) {
    val s = state
    s.regeneratePortfolio(scope) { s.fullPortfolio }
  }

  private data class PortfolioListGenerateResult(
      val portfolio: PortfolioStockList,
      override val all: List<PortfolioStock>,
      override val visible: List<PortfolioStock>,
  ) : ListGenerateResult<PortfolioStock>

  companion object {
    private const val KEY_SEARCH = "search"
  }
}
