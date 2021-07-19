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

package com.pyamsoft.tickertape.portfolio.manage.positions

import androidx.annotation.CheckResult
import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import com.pyamsoft.tickertape.tape.TapeLauncher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PositionsViewModel
@Inject
internal constructor(
    private val tapeLauncher: TapeLauncher,
    private val interactor: PositionsInteractor,
    private val thisHoldingId: DbHolding.Id,
) :
    UiViewModel<PositionsViewState, PositionsControllerEvent>(
        initialState = PositionsViewState(isLoading = false, stock = null)) {

  private val portfolioFetcher =
      highlander<ResultWrapper<PortfolioStock>, Boolean> {
        interactor.getHolding(it, thisHoldingId)
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      interactor.listenForPositionChanges { handlePositionRealtimeEvent(it) }
    }
  }

  private fun CoroutineScope.handlePositionRealtimeEvent(event: PositionChangeEvent) {
    return when (event) {
      is PositionChangeEvent.Delete -> handleDeletePosition(event.position, event.offerUndo)
      is PositionChangeEvent.Insert -> handleInsertPosition(event.position)
      is PositionChangeEvent.Update -> handleUpdatePosition(event.position)
    }
  }

  private fun CoroutineScope.handleUpdatePosition(position: DbPosition) {
    Timber.d("Existing position updated: $position")

    setState {
      copy(
          stock =
              stock?.let { s ->
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                val onlyPositions = s.onlyPositions()
                val existingPosition = onlyPositions.firstOrNull(positionMatchesCallback)
                return@let s.copy(
                    positions =
                        createPositionsList(
                            if (existingPosition == null) {
                              onlyPositions + position
                            } else {
                              onlyPositions.map {
                                if (positionMatchesCallback(it)) position else it
                              }
                            }))
              })
    }
  }

  private fun CoroutineScope.handleInsertPosition(position: DbPosition) {
    Timber.d("New position inserted: $position")

    setState {
      copy(
          stock =
              stock?.let { s ->
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                val onlyPositions = s.onlyPositions()
                val existingPosition = onlyPositions.firstOrNull(positionMatchesCallback)
                return@let s.copy(
                    positions =
                        createPositionsList(
                            if (existingPosition == null) {
                              onlyPositions + position
                            } else {
                              onlyPositions.map {
                                if (positionMatchesCallback(it)) position else it
                              }
                            }))
              })
    }
  }

  private fun CoroutineScope.handleDeletePosition(position: DbPosition, offerUndo: Boolean) {
    Timber.d("Existing position deleted: $position")

    setState {
      copy(
          stock =
              stock?.let { s ->
                val onlyPositions = s.onlyPositions()
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                return@let if (!onlyPositions.contains(positionMatchesCallback)) s
                else {
                  s.copy(
                      positions =
                          createPositionsList(onlyPositions.filterNot(positionMatchesCallback)))
                }
              })
    }
    // TODO offer up undo ability

    // On delete, we don't need to re-fetch quotes from the network
  }

  fun handleFetchPortfolio(force: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchPortfolio(force) }
  }

  private fun CoroutineScope.fetchPortfolio(force: Boolean) {
    launch(context = Dispatchers.Default) {
      setState(
          stateChange = { copy(isLoading = true) },
          andThen = {
            portfolioFetcher
                .call(force)
                .onSuccess { s ->
                  setState {
                    copy(
                        stock =
                            PositionsViewState.PositionStock(
                                holding = s.holding, positions = createPositionsList(s.positions)),
                        isLoading = false)
                  }
                }
                .onFailure { Timber.e(it, "Failed to fetch quotes") }
                .onFailure { setState { copy(stock = null, isLoading = false) } }
          })

      // After the quotes are fetched, start the tape
      tapeLauncher.start()
    }
  }

  fun handleRemove(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val position = state.stock?.positions?.get(index)
      if (position == null) {
        Timber.w("NULL stock, cannot remove position at index: $index")
        return@launch
      }

      if (position !is PositionsViewState.PositionStock.MaybePosition.Position) {
        Timber.w("Not a position at index: $index $position")
        return@launch
      }

      interactor
          .removePosition(position.position.id())
          .onSuccess { Timber.d("Removed position $position") }
          .onFailure { Timber.e(it, "Error removing position $position") }
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun createPositionsList(
        positions: List<DbPosition>
    ): List<PositionsViewState.PositionStock.MaybePosition> {
      if (positions.isEmpty()) {
        return emptyList()
      }

      val totalShares = positions.sumOf { it.shareCount().value() }
      val totalCost = positions.sumOf { it.price().value() * it.shareCount().value() }

      return listOf(PositionsViewState.PositionStock.MaybePosition.Header) +
          positions.map { PositionsViewState.PositionStock.MaybePosition.Position(it) } +
          listOf(
              PositionsViewState.PositionStock.MaybePosition.Footer(
                  totalShares = totalShares.asShares(),
                  totalCost = totalCost.asMoney(),
                  averageCost =
                      if (totalShares.compareTo(0) == 0) StockMoneyValue.none()
                      else (totalCost / totalShares).asMoney()))
    }

    @CheckResult
    private fun PositionsViewState.PositionStock.onlyPositions(): List<DbPosition> {
      return this.positions
          .asSequence()
          .filterIsInstance<PositionsViewState.PositionStock.MaybePosition.Position>()
          .map { it.position }
          .toList()
    }
  }
}
