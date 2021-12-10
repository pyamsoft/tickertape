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

package com.pyamsoft.tickertape.portfolio.dig

import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PortfolioDigViewModeler
@Inject
internal constructor(
    private val state: MutablePortfolioDigViewState,
    interactor: PortfolioDigInteractor,
) :
    DigViewModeler<MutablePortfolioDigViewState>(
        state,
        interactor,
    ) {

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      onLoadTicker(force)
          .onSuccess {
            // Clear the error on load success
            state.error = null
          }
          .onFailure {
            // Don't need to clear the ticker since last loaded state was valid
            state.error = it
          }
          .onFinally { state.isLoading = false }
    }
  }
}
