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

package com.pyamsoft.tickertape.watchlist

import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistDeletePresenter
@Inject
internal constructor(
    private val interactor: WatchlistInteractor,
) {

  fun handleRemove(
      scope: CoroutineScope,
      symbol: StockSymbol,
      onRemoved: () -> Unit,
  ) {
    scope.launch(context = Dispatchers.Main) {
      interactor
          .removeQuote(symbol)
          .onSuccess { Timber.d("Removed symbol $symbol from watchlist") }
          .onFailure { Timber.e(it, "Error removing symbol $symbol from watchlist") }
          .onFinally(onRemoved)
    }
  }
}
