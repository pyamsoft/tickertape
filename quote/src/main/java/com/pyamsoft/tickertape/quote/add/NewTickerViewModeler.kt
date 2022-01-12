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

package com.pyamsoft.tickertape.quote.add

import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class NewTickerViewModeler
@Inject
internal constructor(
    private val state: MutableNewTickerViewState,
    interactor: NewTickerInteractor,
) : AbstractViewModeler<NewTickerViewState>(state) {

  private val lookupRunner =
      highlander<ResultWrapper<List<SearchResult>>, Boolean, String> { force, query ->
        interactor.search(force, query)
      }

  fun handleSymbolChanged(
      scope: CoroutineScope,
      symbol: String,
  ) {
    state.apply {
      this.symbol = symbol
      isLookup = true
      lookupError = null
      lookupResults = emptyList()
    }

    scope.launch(context = Dispatchers.Main) {
      lookupRunner
          .call(false, symbol)
          .onFailure { Timber.e(it, "Error looking up results for $symbol") }
          .onSuccess { Timber.d("Found search results for $symbol $it") }
          .onSuccess { r ->
            state.apply {
              lookupError = null
              lookupResults = r
            }
          }
          .onFailure { e ->
            state.apply {
              lookupError = e
              lookupResults = emptyList()
            }
          }
          .onFinally { state.apply { isLookup = false } }
    }
  }

  private fun MutableNewTickerViewState.cancelInProgressLookup(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      // Cancel any active runner first
      lookupRunner.cancel()

      // Flip all lookup bits back
      isLookup = false
      lookupError = null
      lookupResults = emptyList()
    }
  }

  fun handleEquityTypeSelected(
      scope: CoroutineScope,
      type: EquityType,
  ) {
    state.apply {
      equityType = type
      symbol = ""

      cancelInProgressLookup(scope)
    }
  }

  fun handleClearEquityType(scope: CoroutineScope) {
    state.apply {
      equityType = null
      symbol = ""

      cancelInProgressLookup(scope)
    }
  }
}
