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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.main.MainAdderViewModel
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.ui.AddNew
import com.pyamsoft.tickertape.ui.BottomOffset
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PortfolioViewModel
@Inject
internal constructor(
    private val tapeLauncher: TapeLauncher,
    private val interactor: PortfolioInteractor,
    private val bottomOffsetBus: EventConsumer<BottomOffset>,
    addNewBus: EventConsumer<AddNew>
) :
    MainAdderViewModel<PortfolioViewState, PortfolioControllerEvent>(
        addNewBus = addNewBus,
        initialState =
            PortfolioViewState(
                error = null, isLoading = false, portfolio = emptyList(), bottomOffset = 0)) {

  private val portfolioFetcher =
      highlander<Unit, Boolean> { force ->
        setState(
            stateChange = { copy(isLoading = true) },
            andThen = {
              interactor
                  .getPortfolio(force)
                  .onSuccess { setState { copy(error = null, portfolio = it, isLoading = false) } }
                  .onFailure { Timber.e(it, "Failed to fetch quotes") }
                  .onFailure { setState { copy(error = it, isLoading = false) } }
            })
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      interactor.listenForHoldingChanges { handleHoldingRealtimeEvent(it) }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      interactor.listenForPositionChanges { handlePositionRealtimeEvent(it) }
    }
  }

  override fun CoroutineScope.onAddNewEvent() {
    Timber.d("Portfolio add new holding!")
    publish(PortfolioControllerEvent.AddNewHolding)
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
          portfolio =
              portfolio.map { stock ->
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                val existingPosition = stock.positions.firstOrNull(positionMatchesCallback)
                return@map stock.copy(
                    positions =
                        if (existingPosition == null) {
                          stock.positions + position
                        } else {
                          stock.positions.map { if (positionMatchesCallback(it)) position else it }
                        })
              })
    }
  }

  private fun CoroutineScope.handleInsertPosition(position: DbPosition) {
    Timber.d("New position inserted: $position")

    setState {
      copy(
          portfolio =
              portfolio.map { stock ->
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                val existingPosition = stock.positions.firstOrNull(positionMatchesCallback)
                return@map stock.copy(
                    positions =
                        if (existingPosition == null) {
                          stock.positions + position
                        } else {
                          stock.positions.map { if (positionMatchesCallback(it)) position else it }
                        })
              })
    }
  }

  private fun CoroutineScope.handleDeletePosition(position: DbPosition, offerUndo: Boolean) {
    Timber.d("Existing position deleted: $position")

    setState {
      copy(
          portfolio =
              portfolio.map { stock ->
                val positionMatchesCallback = { p: DbPosition -> p.id() == position.id() }
                return@map if (!stock.positions.contains(positionMatchesCallback)) stock
                else {
                  stock.copy(positions = stock.positions.filterNot(positionMatchesCallback))
                }
              })
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

  private fun CoroutineScope.handleUpdateHolding(holding: DbHolding) {
    Timber.d("Existing holding updated: $holding")

    // Don't actually update anything in the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    fetchPortfolio(true)
  }

  private fun CoroutineScope.handleInsertHolding(holding: DbHolding) {
    Timber.d("New holding inserted: $holding")

    // Don't actually update anything in the list here, but call a full refresh
    // This will re-fetch the DB and the network and give us back quotes
    fetchPortfolio(true)
  }

  private fun CoroutineScope.handleDeleteHolding(holding: DbHolding, offerUndo: Boolean) {
    Timber.d("Existing holding deleted: $holding")

    setState { copy(portfolio = portfolio.filterNot { it.holding.id() == holding.id() }) }
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
      val stock = state.portfolio[index]
      interactor.removeHolding(stock.holding.id())
    }
  }

  fun handleManageHolding(index: Int) {
    viewModelScope.launch(context = Dispatchers.Default) {
      val stock = state.portfolio[index]
      publish(PortfolioControllerEvent.ManageHolding(stock))
    }
  }
}
