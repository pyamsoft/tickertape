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

import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.tickertape.ui.UiTabs
import javax.inject.Inject

class WatchlistTabs @Inject internal constructor(appBarActivity: AppBarActivity) :
    UiTabs<WatchListViewState, WatchListViewEvent>(appBarActivity) {

  override fun handleOptionsTabSelected() {
    publish(WatchListViewEvent.ShowOptions)
  }

  override fun handleStocksTabSelected() {
    publish(WatchListViewEvent.ShowStocks)
  }

  override fun render(state: UiRender<WatchListViewState>) {
    state.mapChanged { it.section }.render(viewScope) { handleSection(it) }
  }
}
