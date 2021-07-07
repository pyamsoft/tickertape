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

package com.pyamsoft.tickertape.quote.ui.chart

import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.quote.ui.databinding.QuoteChartBinding
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.robinhood.spark.SparkAdapter
import com.robinhood.spark.SparkView
import com.robinhood.spark.animation.MorphSparkAnimator
import javax.inject.Inject
import timber.log.Timber

class QuoteChartView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<QuoteChartViewState, QuoteChartViewEvent, QuoteChartBinding>(parent) {

  override val layoutRoot by boundView { watchlistDigChart }

  override val viewBinding = QuoteChartBinding::inflate

  private var adapter: ChartAdapter? = null

  init {
    doOnInflate { inflateChart() }

    doOnInflate { inflateRanges() }

    doOnTeardown {
      binding.watchlistDigChart.scrubListener = null
      clearAdapter()
    }
  }

  private fun inflateRanges() {}

  private fun inflateChart() {
    // Setup scrub listener
    binding.watchlistDigChart.setScrubListener { raw ->
      val data = raw as? ChartData
      if (data == null) {
        clearScrubView()
      } else {
        handleScrubbedView(data)
      }
    }

    // Setup Chart visual
    binding.watchlistDigChart.apply {
      isScrubEnabled = true
      fillType = SparkView.FillType.TOWARD_ZERO
      lineColor =
          Color.argb(
              (0.6 * 255).toInt(),
              Color.red(DEFAULT_STOCK_UP_COLOR),
              Color.green(DEFAULT_STOCK_UP_COLOR),
              Color.blue(DEFAULT_STOCK_UP_COLOR))
      scrubLineColor = DEFAULT_STOCK_COLOR
      baseLineColor = DEFAULT_STOCK_COLOR

      sparkAnimator = MorphSparkAnimator()
    }
  }

  private fun handleScrubbedView(data: ChartData) {
    publish(QuoteChartViewEvent.Scrub(data))
  }

  private fun clearScrubView() {}

  private fun clearAdapter() {
    binding.watchlistDigChart.adapter = null
    adapter = null
  }

  override fun onRender(state: UiRender<QuoteChartViewState>) {
    state.mapChanged { it.chart }.render(viewScope) { handleChartChanged(it) }
  }

  private fun handleChartChanged(chart: StockChart?) {
    clearAdapter()

    if (chart == null) {
      // Chart error
      Timber.w("Failed to load chart")
    } else {
      // Load was successful, we have required data
      adapter = ChartAdapter(chart).also { binding.watchlistDigChart.adapter = it }
    }
  }

  private class ChartAdapter(chart: StockChart) : SparkAdapter() {

    private val chartData: List<ChartData>
    private val baselineValue: Float = chart.startingPrice().value().toFloat()

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

    @CheckResult
    private fun getValue(item: ChartData): Double {
      return item.close.value()
    }

    override fun getCount(): Int {
      return chartData.size
    }

    override fun getItem(index: Int): ChartData {
      return chartData[index]
    }

    override fun getY(index: Int): Float {
      // Offset the Y based on the baseline value which is either previous close or day's open
      return getValue(getItem(index)).toFloat() - baselineValue
    }

    override fun hasBaseLine(): Boolean {
      return true
    }

    override fun getBaseLine(): Float {
      // Baseline of 0 to show the chart dipping below zero
      return 0F
    }
  }
}
