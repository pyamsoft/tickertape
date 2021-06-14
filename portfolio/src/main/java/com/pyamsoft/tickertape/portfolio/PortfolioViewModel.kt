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
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.onActualError
import com.pyamsoft.pydroid.bus.EventConsumer
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
    private val addNewBus: EventConsumer<AddNew>
) :
    UiViewModel<PortfolioViewState, PortfolioControllerEvent>(
        initialState =
            PortfolioViewState(
                error = null, isLoading = false, portfolio = emptyList(), bottomOffset = 0)) {

  private val portfolioFetcher =
      highlander<Unit, Boolean> { force ->
        setState(
            stateChange = { copy(isLoading = true) },
            andThen = {
              try {
                val portfolio = interactor.getPortfolio(force)
                setState { copy(error = null, portfolio = portfolio, isLoading = false) }
              } catch (error: Throwable) {
                error.onActualError { e ->
                  Timber.e(e, "Failed to fetch quotes")
                  setState { copy(error = e, isLoading = false) }
                }
              }
            })
      }

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      addNewBus.onEvent { publish(PortfolioControllerEvent.AddNewHolding) }
    }
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
}
