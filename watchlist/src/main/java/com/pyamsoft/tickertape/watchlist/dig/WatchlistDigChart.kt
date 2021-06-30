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
import androidx.core.content.withStyledAttributes
import androidx.core.view.updatePadding
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.quote.QuotedChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import com.pyamsoft.tickertape.watchlist.R
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistDigChartBinding
import com.robinhood.spark.SparkAdapter
import com.robinhood.spark.SparkView
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject
import timber.log.Timber

class WatchlistDigChart @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<WatchListDigViewState, WatchListDigViewEvent, WatchlistDigChartBinding>(parent) {

  override val layoutRoot by boundView { watchlistDigChartRoot }

  override val viewBinding = WatchlistDigChartBinding::inflate

  private var adapter: ChartAdapter? = null

  init {
    doOnInflate {
      layoutRoot.context.withStyledAttributes(attrs = intArrayOf(R.attr.actionBarSize)) {
        // Offset the container by the action bar size
        val height = getDimensionPixelSize(0, 0)
        layoutRoot.updatePadding(top = layoutRoot.paddingTop + height)
      }
    }

    doOnTeardown { clearAdapter() }

    doOnInflate {
      binding.watchlistDigChartSpark.setScrubListener { raw ->
        val data = raw as? ChartData
        if (data == null) {
          clearScrubView()
        } else {
          handleScrubbedView(data)
        }
      }
    }

    doOnTeardown { binding.watchlistDigChartSpark.scrubListener = null }

    doOnInflate {
      binding.watchlistDigChartSpark.apply {
        isScrubEnabled = true
        fillType = SparkView.FillType.TOWARD_ZERO
        isFill
        scrubLineColor = DEFAULT_STOCK_UP_COLOR
        lineColor = DEFAULT_STOCK_UP_COLOR
        baseLineColor = DEFAULT_STOCK_UP_COLOR
      }
    }
  }

  private fun handleScrubbedView(data: ChartData) {
    Timber.d("Chart scrubbed: $data")
  }

  private fun clearScrubView() {}

  private fun clearAdapter() {
    binding.watchlistDigChartSpark.adapter = null
    adapter = null
  }

  override fun onRender(state: UiRender<WatchListDigViewState>) {
    state.mapChanged { it.chart }.render(viewScope) { handleChartChanged(it) }
  }

  private fun handleChartChanged(chart: QuotedChart?) {
    clearAdapter()
    if (chart != null) {
      val c = chart.chart
      if (c != null) {
        adapter = ChartAdapter(c).also { binding.watchlistDigChartSpark.adapter = it }
      }
    }
  }

  private class ChartAdapter(chart: StockChart) : SparkAdapter() {

    private val chartData: List<ChartData>
    private val offset = OffsetDateTime.now().offset

    init {
      val dates = chart.dates()
      val opens = chart.open()
      val closes = chart.close()
      val highs = chart.high()
      val lows = chart.low()
      val volumes = chart.volume()

      val data = mutableListOf<ChartData>()
      for (i in dates.indices) {
        val date = dates[i]
        val open = opens[i]
        val close = closes[i]
        val high = highs[i]
        val low = lows[i]
        val volume = volumes[i]
        data.add(
            ChartData(
                date = date, open = open, close = close, high = high, low = low, volume = volume))
      }
      chartData = data
    }

    override fun getCount(): Int {
      return chartData.size
    }

    override fun getItem(index: Int): ChartData {
      return chartData[index]
    }

    override fun getY(index: Int): Float {
      return getItem(index).open.value().toFloat()
    }
  }

  private data class ChartData(
      val date: LocalDateTime,
      val volume: StockVolumeValue,
      val open: StockMoneyValue,
      val close: StockMoneyValue,
      val high: StockMoneyValue,
      val low: StockMoneyValue
  )
}
