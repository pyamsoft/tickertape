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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.util.asDp
import com.pyamsoft.spark.SparkAdapter
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.quote.ui.databinding.QuoteChartBinding
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.periodHigh
import com.pyamsoft.tickertape.stocks.api.periodLow
import com.pyamsoft.tickertape.ui.getUserMessage
import timber.log.Timber

abstract class QuoteChartView<S : UiViewState, V : UiViewEvent>
protected constructor(parent: ViewGroup) : BaseUiView<S, V, QuoteChartBinding>(parent) {

  final override val layoutRoot by boundView { quoteChartRoot }

  final override val viewBinding = QuoteChartBinding::inflate

  init {
    doOnInflate { inflateChart() }

    doOnInflate { inflateRanges() }

    doOnTeardown { clearAdapter() }

    doOnTeardown { clearBounds() }
  }

  private fun clearBounds() {
    binding.quoteChartTop.text = null
    binding.quoteChartBottom.text = null
  }

  private fun inflateRanges() {}

  private fun inflateChart() {
    // Setup Chart visual
    binding.quoteChart.apply {
      isFilled = true

      positiveLineColor =
          Color.argb(
              255,
              Color.red(DEFAULT_STOCK_UP_COLOR),
              Color.green(DEFAULT_STOCK_UP_COLOR),
              Color.blue(DEFAULT_STOCK_UP_COLOR))

      positiveFillColor =
          Color.argb(
              (0.5 * 255).toInt(),
              Color.red(DEFAULT_STOCK_UP_COLOR),
              Color.green(DEFAULT_STOCK_UP_COLOR),
              Color.blue(DEFAULT_STOCK_UP_COLOR))

      negativeLineColor =
          Color.argb(
              255,
              Color.red(DEFAULT_STOCK_DOWN_COLOR),
              Color.green(DEFAULT_STOCK_DOWN_COLOR),
              Color.blue(DEFAULT_STOCK_DOWN_COLOR))

      negativeFillColor =
          Color.argb(
              (0.5 * 255).toInt(),
              Color.red(DEFAULT_STOCK_DOWN_COLOR),
              Color.green(DEFAULT_STOCK_DOWN_COLOR),
              Color.blue(DEFAULT_STOCK_DOWN_COLOR))

      scrubLineColor = DEFAULT_STOCK_COLOR
      baseLineColor = DEFAULT_STOCK_COLOR
      baseLineWidth = 2.asDp(layoutRoot.context).toFloat()
    }
  }

  private fun clearAdapter() {
    binding.quoteChart.adapter = null
  }

  protected fun handleRender(state: UiRender<QuoteChartViewState>) {
    state.mapChanged { it.chart }.render(viewScope) { handleChartChanged(it) }
    state.mapChanged { it.error }.render(viewScope) { handleError(it) }
  }

  private fun handleError(throwable: Throwable?) {
    if (throwable == null) {
      binding.apply {
        quoteChartError.isGone = true
        quoteChartData.isVisible = true
      }
    } else {
      binding.apply {
        quoteChartData.isGone = true
        quoteChartError.isVisible = true
        quoteChartError.text = throwable.getUserMessage()
      }
    }
  }

  private fun handleChartChanged(chart: StockChart?) {
    clearAdapter()

    if (chart == null) {
      // Chart error
      Timber.w("Failed to load chart")
      clearBounds()
    } else {
      // Load was successful, we have required data
      binding.apply {
        val high = chart.periodHigh()
        val low = chart.periodLow()
        quoteChart.adapter = ChartAdapter(chart, high, low)
        quoteChartTop.text = high.asMoneyValue()
        quoteChartBottom.text = low.asMoneyValue()
      }
    }
  }

  private class ChartAdapter(chart: StockChart, high: StockMoneyValue, low: StockMoneyValue) :
      SparkAdapter() {

    private val chartData: List<ChartData>
    private val baselineValue: Float

    init {
      val dates = chart.dates()
      val closes = chart.close()

      val data = mutableListOf<ChartData>()
      val baseline = chart.startingPrice()

      for (i in dates.indices) {
        val date = dates[i]
        val close = closes[i]
        data.add(
            ChartData(
                high = high,
                low = low,
                baseline = baseline,
                range = chart.range(),
                date = date,
                price = close))
      }

      baselineValue = baseline.value().toFloat()
      chartData = data
    }

    @CheckResult
    private fun getValue(item: ChartData): Double {
      return item.price.value()
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

    override fun getBaseLine(): Float {
      // Baseline of 0 to show the chart dipping below zero
      return 0F
    }
  }
}
