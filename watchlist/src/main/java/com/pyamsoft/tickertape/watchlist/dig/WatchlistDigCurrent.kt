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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.ui.chart.ChartData
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistDigCurrentBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class WatchlistDigCurrent @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<WatchListDigViewState, WatchListDigViewEvent, WatchlistDigCurrentBinding>(parent) {

  override val layoutRoot by boundView { watchlistDigCurrent }

  override val viewBinding = WatchlistDigCurrentBinding::inflate

  init {
    doOnTeardown { clear() }
  }

  override fun onRender(state: UiRender<WatchListDigViewState>) {
    state.mapChanged { it.scrub }.render(viewScope) { handleScrubChanged(it) }
  }

  private fun clear() {
    binding.apply {
      watchlistDigCurrentDate.text = null
      watchlistDigCurrentPrice.text = null
    }
  }

  private fun handleScrubChanged(data: ChartData?) {
    if (data == null) {
      clear()
    } else {
      binding.apply {
        val formatter =
            if (data.range < StockChart.IntervalRange.THREE_MONTH) FORMATTER_WITH_TIME
            else FORMATTER
        watchlistDigCurrentDate.text = formatter.get().requireNotNull().format(data.date)
        watchlistDigCurrentPrice.text = data.price.asMoneyValue()
      }
    }
  }

  companion object {

    private val FORMATTER =
        object : ThreadLocal<DateTimeFormatter>() {

          override fun initialValue(): DateTimeFormatter {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
          }
        }

    private val FORMATTER_WITH_TIME =
        object : ThreadLocal<DateTimeFormatter>() {

          override fun initialValue(): DateTimeFormatter {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
          }
        }
  }
}
