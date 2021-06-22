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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.arch.UiSavedStateViewModelProvider
import com.pyamsoft.pydroid.arch.onActualError
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.tape.TapeLauncher
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PositionsViewModel
@AssistedInject
internal constructor(
    @Assisted savedState: UiSavedState,
    private val tapeLauncher: TapeLauncher,
    private val interactor: PositionsInteractor,
    private val thisHoldingId: DbHolding.Id,
) :
    UiSavedStateViewModel<PositionsViewState, PositionsControllerEvent>(
        savedState,
        initialState =
            PositionsViewState(
                isLoading = false,
                numberOfShares = StockShareValue.none(),
                pricePerShare = StockMoneyValue.none(),
                stock = null)
    ) {

  private val portfolioFetcher =
      highlander<Unit, Boolean> { force ->
        setState(
            stateChange = { copy(isLoading = true) },
            andThen = {
              try {
                val maybeStock = interactor.getHolding(force, thisHoldingId)
                setState { copy(stock = maybeStock, isLoading = false) }
              } catch (error: Throwable) {
                error.onActualError { e ->
                  Timber.e(e, "Failed to fetch quotes")
                  setState { copy(stock = null, isLoading = false) }
                }
              }
            })
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
                val existingPosition = s.positions.firstOrNull(positionMatchesCallback)
                return@let s.copy(
                    positions =
                        if (existingPosition == null) {
                          s.positions + position
                        } else {
                          s.positions.map { if (positionMatchesCallback(it)) position else it }
                        })
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
                val existingPosition = s.positions.firstOrNull(positionMatchesCallback)
                return@let s.copy(
                    positions =
                        if (existingPosition == null) {
                          s.positions + position
                        } else {
                          s.positions.map { if (positionMatchesCallback(it)) position else it }
                        })
              })
    }
  }

  private fun CoroutineScope.handleDeletePosition(position: DbPosition, offerUndo: Boolean) {
    Timber.d("Existing position deleted: $position")

    setState {
      copy(
          stock =
              stock?.let { s ->
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                return@let if (!s.positions.contains(positionMatchesCallback)) s
                else {
                  s.copy(positions = s.positions.filterNot(positionMatchesCallback))
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
      portfolioFetcher.call(force)

      // After the quotes are fetched, start the tape
      tapeLauncher.start()
    }
  }

  fun handleRemove(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val position = state.stock?.positions?.get(index)
      if (position == null) {
        Timber.w("No position at index: $index")
        return@launch
      }

      interactor.removePosition(position.id())
    }
  }

  fun handleUpdateNumberOfShares(number: StockShareValue) {
    setState { copy(numberOfShares = number) }
  }

  fun handleUpdateSharePrice(price: StockMoneyValue) {
    setState { copy(pricePerShare = price) }
  }

  fun handleCreatePosition() {
    val sharePrice = state.pricePerShare
    val shareCount = state.numberOfShares
    setState(
        stateChange = {
          copy(pricePerShare = StockMoneyValue.none(), numberOfShares = StockShareValue.none())
        },
        andThen = { newState ->
          val stock = newState.stock
          if (stock == null) {
            Timber.w("Cannot create new position, holding is invalid")
            return@setState
          }

          interactor.createPosition(
              id = stock.holding.id(), numberOfShares = shareCount, pricePerShare = sharePrice)
        })
  }

  @AssistedFactory
  interface Factory : UiSavedStateViewModelProvider<PositionsViewModel> {
    override fun create(savedState: UiSavedState): PositionsViewModel
  }
}
