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
import com.pyamsoft.pydroid.arch.*
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.quote.TickerTabs
import com.pyamsoft.tickertape.stocks.api.EquityType
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

  private var fullPortfolio: List<PortfolioStock> = emptyList()

  private val portfolioFetcher =
      highlander<ResultWrapper<List<PortfolioStock>>, Boolean> { interactor.getPortfolio(it) }

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForHoldingChanges { handleHoldingRealtimeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) {
      interactor.listenForPositionChanges { handlePositionRealtimeEvent(it) }
    }
  }

  private fun handlePositionRealtimeEvent(event: PositionChangeEvent) {
    return when (event) {
      is PositionChangeEvent.Delete -> handleDeletePosition(event.position, event.offerUndo)
      is PositionChangeEvent.Insert -> handleInsertPosition(event.position)
      is PositionChangeEvent.Update -> handleUpdatePosition(event.position)
    }
  }

  private fun handleUpdatePosition(position: DbPosition) {
    val doesPositionMatch = { p: DbPosition -> p.id() == position.id() }

    val newPortfolio =
        fullPortfolio.map { stock ->
          return@map if (stock.holding.id() != position.holdingId()) stock
          else {
            val newPositions = stock.positions.map { if (doesPositionMatch(it)) position else it }
            stock.copy(positions = newPositions)
          }
        }
    state.regeneratePortfolio(newPortfolio)
  }

  private fun handleInsertPosition(position: DbPosition) {
    val newPortfolio =
        fullPortfolio.map { stock ->
          return@map if (stock.holding.id() != position.holdingId()) stock
          else stock.copy(positions = stock.positions + position)
        }
    state.regeneratePortfolio(newPortfolio)
  }

  private fun handleDeletePosition(position: DbPosition, offerUndo: Boolean) {
    val doesPositionMatch = { p: DbPosition -> p.id() == position.id() }

    val newPortfolio =
        fullPortfolio.map { stock ->
          if (stock.holding.id() != position.holdingId()) {
            return@map stock
          } else {
            return@map if (!stock.positions.contains(doesPositionMatch)) stock
            else {
              stock.copy(positions = stock.positions.filterNot(doesPositionMatch))
            }
          }
        }
    state.regeneratePortfolio(newPortfolio)
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

  private fun MutablePortfolioViewState.regeneratePortfolio(tickers: List<PortfolioStock>) {
    fullPortfolio = tickers.sortedWith(PortfolioStock.COMPARATOR)
    this.portfolio = PortfolioStockList.of(fullPortfolio)
    this.stocks = asVisible(fullPortfolio)
  }

  @CheckResult
  private fun PortfolioViewState.asVisible(tickers: List<PortfolioStock>): List<PortfolioStock> {
    val search = this.query
    val section = this.section
    return tickers
        .asSequence()
        .filter { ps ->
          val symbol = ps.holding.symbol().symbol()
          val name = ps.ticker?.quote?.company()?.company()
          return@filter if (symbol.contains(search, ignoreCase = true)) true
          else name?.contains(search, ignoreCase = true) ?: false
        }
        .filter { ps ->
          val type = ps.holding.type()
          return@filter when (section) {
            TickerTabs.STOCKS -> type == EquityType.STOCK
            TickerTabs.OPTIONS -> type == EquityType.OPTION
            TickerTabs.CRYPTO -> type == EquityType.CRYPTOCURRENCY
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

  private fun handleDeleteHolding(holding: DbHolding, offerUndo: Boolean) {
    val newPortfolio = fullPortfolio.filterNot { it.holding.id() == holding.id() }
    state.regeneratePortfolio(newPortfolio)
    // TODO offer up undo ability

    // On delete, we don't need to re-fetch quotes from the network
  }

  fun handleRefreshList(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      portfolioFetcher
          .call(force)
          .onSuccess {
            state.apply {
              regeneratePortfolio(it)
              error = null
            }
          }
          .onFailure { Timber.e(it, "Failed to refresh entry list") }
          .onFailure {
            state.apply {
              regeneratePortfolio(emptyList())
              error = it
            }
          }
          .onFinally { state.isLoading = false }
    }
  }

  fun handleRemove(scope: CoroutineScope, stock: PortfolioStock) {
    scope.launch(context = Dispatchers.Main) {
      interactor
          .removeHolding(stock.holding)
          .onSuccess { Timber.d("Removed holding $stock") }
          .onFailure { Timber.e(it, "Error removing holding: $stock") }
    }
  }

  fun handleSearch(query: String) {
    state.apply {
      this.query = query
      regeneratePortfolio(fullPortfolio)
    }
  }

  fun handleSectionChanged(tab: TickerTabs) {
    state.apply {
      this.section = tab
      regeneratePortfolio(fullPortfolio)
    }
  }

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    savedInstanceState.get<String>(KEY_SEARCH)?.also { search -> state.query = search }
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

  companion object {
    private const val KEY_SEARCH = "search"
  }
}
