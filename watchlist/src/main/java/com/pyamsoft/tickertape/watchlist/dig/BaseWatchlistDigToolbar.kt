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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.UiDialogToolbar
import javax.inject.Inject

class BaseWatchlistDigToolbar
@Inject
internal constructor(
    imageLoader: ImageLoader,
    parent: ViewGroup,
) : UiDialogToolbar<BaseWatchListDigViewState, BaseWatchListDigViewEvent>(imageLoader, parent) {

  init {
    doOnInflate {
      binding.uiToolbar.setNavigationOnClickListener(
          DebouncedOnClickListener.create { publish(BaseWatchListDigViewEvent.Close) })
    }

    doOnTeardown { clear() }
  }

  private fun clear() {
    binding.uiToolbar.setNavigationOnClickListener(null)
  }

  override fun onRender(state: UiRender<BaseWatchListDigViewState>) {
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbolChanged(it) }
  }

  private fun handleSymbolChanged(symbol: StockSymbol) {
    binding.uiToolbar.title = symbol.symbol()
  }
}
