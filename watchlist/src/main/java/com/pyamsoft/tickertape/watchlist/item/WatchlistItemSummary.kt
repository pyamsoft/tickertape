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

package com.pyamsoft.tickertape.watchlist.item

import android.view.ViewGroup
import androidx.core.view.isInvisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistItemSummaryBinding
import javax.inject.Inject

class WatchlistItemSummary @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<WatchlistItemViewState.Item, WatchlistItemViewEvent, WatchlistItemSummaryBinding>(
        parent) {

  override val viewBinding = WatchlistItemSummaryBinding::inflate

  override val layoutRoot by boundView { watchlistItemSummaryRoot }

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<WatchlistItemViewState.Item>) {
    state.mapChanged { it.quote }.apply {
      mapChanged { it?.dayOpen() }.render(viewScope) { handleDayOpenChanged(it) }
      mapChanged { it?.dayPreviousClose() }.render(viewScope) { handleDayCloseChanged(it) }
      mapChanged { it?.dayHigh() }.render(viewScope) { handleDayHighChanged(it) }
      mapChanged { it?.dayLow() }.render(viewScope) { handleDayLowChanged(it) }
      mapChanged { it?.dayVolume() }.render(viewScope) { handleDayVolumeChanged(it) }
    }
  }

  private fun handleDayVolumeChanged(volume: StockVolumeValue?) {
    binding.apply {
      val missing = volume == null
      watchlistItemSummaryVolumeLabel.isInvisible = missing
      watchlistItemSummaryVolumeText.isInvisible = missing

      if (volume != null) {
        watchlistItemSummaryVolumeText.text = volume.asVolumeValue()
      }
    }
  }

  private fun handleDayLowChanged(low: StockMoneyValue?) {
    binding.apply {
      val missing = low == null
      watchlistItemSummaryLowLabel.isInvisible = missing
      watchlistItemSummaryLowText.isInvisible = missing

      if (low != null) {
        watchlistItemSummaryLowText.text = low.asMoneyValue()
      }
    }
  }

  private fun handleDayHighChanged(high: StockMoneyValue?) {
    binding.apply {
      val missing = high == null
      watchlistItemSummaryHighLabel.isInvisible = missing
      watchlistItemSummaryHighText.isInvisible = missing

      if (high != null) {
        watchlistItemSummaryHighText.text = high.asMoneyValue()
      }
    }
  }

  private fun handleDayCloseChanged(close: StockMoneyValue?) {
    binding.apply {
      val missing = close == null
      watchlistItemSummaryCloseLabel.isInvisible = missing
      watchlistItemSummaryCloseText.isInvisible = missing

      if (close != null) {
        watchlistItemSummaryCloseText.text = close.asMoneyValue()
      }
    }
  }

  private fun handleDayOpenChanged(open: StockMoneyValue?) {
    binding.apply {
      val missing = open == null
      watchlistItemSummaryOpenLabel.isInvisible = missing
      watchlistItemSummaryOpenText.isInvisible = missing

      if (open != null) {
        watchlistItemSummaryOpenText.text = open.asMoneyValue()
      }
    }
  }

  private fun clear() {
    binding.watchlistItemSummaryVolumeText.text = ""
    binding.watchlistItemSummaryLowText.text = ""
    binding.watchlistItemSummaryHighText.text = ""
    binding.watchlistItemSummaryOpenText.text = ""
    binding.watchlistItemSummaryCloseText.text = ""
  }
}
