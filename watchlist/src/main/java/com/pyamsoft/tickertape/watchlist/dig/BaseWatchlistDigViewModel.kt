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

import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class BaseWatchlistDigViewModel @Inject internal constructor(thisSymbol: StockSymbol) :
    UiViewModel<BaseWatchListDigViewState, BaseWatchListDigControllerEvent>(
        initialState = BaseWatchListDigViewState(page = DEFAULT_PAGE, symbol = thisSymbol)) {

  fun handleLoadDefaultPage() {
    loadPage(DEFAULT_PAGE)
  }

  private fun publishPage() {
    return when (state.page) {
      WatchlistDigPages.QUOTE -> publish(BaseWatchListDigControllerEvent.PushQuote)
    }
  }

  private fun loadPage(page: WatchlistDigPages) {
    setState(stateChange = { copy(page = page) }, andThen = { publishPage() })
  }

  companion object {

    private val DEFAULT_PAGE = WatchlistDigPages.QUOTE
  }
}
