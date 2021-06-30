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

package com.pyamsoft.tickertape.watchlist.dig

import androidx.annotation.CheckResult
import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.util.Optional
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistDigViewModel
@Inject
internal constructor(interactor: WatchlistDigInteractor, thisSymbol: StockSymbol) :
    UiViewModel<WatchListDigViewState, WatchListDigControllerEvent>(
        initialState =
            WatchListDigViewState(symbol = thisSymbol, isLoading = false, stock = null)) {

  private val quoteFetcher =
      highlander<ResultWrapper<QuoteWithChart>, Boolean, StockChart.IntervalRange> { force, range ->
        interactor.getQuoteWithChart(force, thisSymbol, range)
      }

  fun handleFetchQuote(force: Boolean, range: StockChart.IntervalRange) {
    viewModelScope.launch(context = Dispatchers.Default) { fetchQuote(force, range) }
  }

  private fun CoroutineScope.fetchQuote(force: Boolean, range: StockChart.IntervalRange) {
    launch(context = Dispatchers.Default) {
      setState(
          stateChange = { copy(isLoading = true) },
          andThen = {
            quoteFetcher
                .call(force, range)
                .onSuccess { setState { copy(stock = it.asOptional(), isLoading = false) } }
                .onFailure { Timber.e(it, "Failed to fetch quote with stock") }
                .onFailure { setState { copy(stock = null.asOptional(), isLoading = false) } }
          })
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun <T : Any> T?.asOptional(): Optional<T> {
      return Optional.ofNullable(this)
    }
  }
}
