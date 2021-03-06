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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import javax.inject.Inject

interface WatchlistViewState : UiViewState {
  val query: String
  val section: EquityType
  val isLoading: Boolean
  val watchlist: List<Ticker>
  val error: Throwable?
}

@ActivityScope
internal class MutableWatchlistViewState @Inject internal constructor() : WatchlistViewState {
  override var query by mutableStateOf("")
  override var section by mutableStateOf(EquityType.STOCK)
  override var isLoading by mutableStateOf(false)
  override var watchlist by mutableStateOf(emptyList<Ticker>())
  override var error by mutableStateOf<Throwable?>(null)

  // Used in WatchlistViewModeler to track all tickers, even non visible
  internal var allTickers by mutableStateOf(emptyList<Ticker>())
}
