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

package com.pyamsoft.tickertape.quote

import android.graphics.Color
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pyamsoft.spark.SparkAdapter
import com.pyamsoft.spark.SparkView
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.periodHigh
import com.pyamsoft.tickertape.stocks.api.periodLow
import java.time.LocalDateTime

@Composable
@JvmOverloads
fun Chart(
    modifier: Modifier = Modifier,
    chart: StockChart,
    onScrub: ((Chart.Data?) -> Unit)? = null,
) {
  Box(
      modifier = modifier,
  ) {
    SparkChart(
        modifier = Modifier.padding(start = 8.dp),
        chart = chart,
        onScrub = onScrub,
    )
    Bounds(
        modifier = Modifier.matchParentSize(),
        chart = chart,
    )
  }
}

@Composable
private fun Bounds(
    modifier: Modifier = Modifier,
    chart: StockChart,
) {
  val highMoney = remember(chart) { chart.periodHigh().asMoneyValue() }
  val lowMoney = remember(chart) { chart.periodLow().asMoneyValue() }

  Column(
      modifier = modifier,
  ) {
    ChartBound(
        value = highMoney,
    )
    Spacer(
        modifier = Modifier.weight(1F),
    )
    ChartBound(
        value = lowMoney,
    )
  }
}

@Composable
private fun ChartBound(
    modifier: Modifier = Modifier,
    value: String,
) {
  Surface(
      modifier = modifier,
      shape = MaterialTheme.shapes.small.copy(CornerSize(4.dp)),
      color = MaterialTheme.colors.primary,
      contentColor = MaterialTheme.colors.onSurface,
  ) {
    Text(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        text = value,
        style =
            MaterialTheme.typography.caption.copy(
                fontSize = 10.sp,
            ),
    )
  }
}

@Composable
private fun SparkChart(
    modifier: Modifier = Modifier,
    chart: StockChart,
    onScrub: ((Chart.Data?) -> Unit)?,
) {
  val density = LocalDensity.current

  val baseLineSize = remember(density) { density.run { 2.dp.toPx() } }

  val chartAdapter =
      remember(chart) {
        val high = chart.periodHigh()
        val low = chart.periodLow()
        ChartAdapter(chart, high, low)
      }

  val onScrubListener =
      if (onScrub == null) null
      else remember(onScrub) { SparkView.OnScrubListener { onScrub(it as? Chart.Data) } }

  AndroidView(
      modifier = modifier,
      factory = { context ->
        SparkView(context).apply {
          isFilled = true

          positiveLineColor =
              Color.argb(
                  255,
                  Color.red(DEFAULT_STOCK_UP_COLOR),
                  Color.green(DEFAULT_STOCK_UP_COLOR),
                  Color.blue(DEFAULT_STOCK_UP_COLOR),
              )

          positiveFillColor =
              Color.argb(
                  (0.5 * 255).toInt(),
                  Color.red(DEFAULT_STOCK_UP_COLOR),
                  Color.green(DEFAULT_STOCK_UP_COLOR),
                  Color.blue(DEFAULT_STOCK_UP_COLOR),
              )

          negativeLineColor =
              Color.argb(
                  255,
                  Color.red(DEFAULT_STOCK_DOWN_COLOR),
                  Color.green(DEFAULT_STOCK_DOWN_COLOR),
                  Color.blue(DEFAULT_STOCK_DOWN_COLOR),
              )

          negativeFillColor =
              Color.argb(
                  (0.5 * 255).toInt(),
                  Color.red(DEFAULT_STOCK_DOWN_COLOR),
                  Color.green(DEFAULT_STOCK_DOWN_COLOR),
                  Color.blue(DEFAULT_STOCK_DOWN_COLOR),
              )

          scrubLineColor = DEFAULT_STOCK_COLOR
          baseLineColor = DEFAULT_STOCK_COLOR
        }
      },
      update = { sparkView ->
        sparkView.apply {

          // Guard to avoid Android View invalidate
          if (baseLineWidth != baseLineSize) {
            baseLineWidth = baseLineSize
          }

          // Guard to avoid Android View invalidate
          if (adapter != chartAdapter) {
            adapter = chartAdapter
          }

          // Guard to avoid Android View invalidate
          if (scrubListener != onScrubListener) {
            scrubListener = onScrubListener

            // Once we have set the listener, if scrubbing is not enabled, turn it on, else turn it
            // off
            if (onScrubListener != null) {
              if (!isScrubEnabled) {
                isScrubEnabled = true
              }
            } else {
              if (isScrubEnabled) {
                isScrubEnabled = false
              }
            }
          }
        }
      },
  )
}

private class ChartAdapter(
    chart: StockChart,
    high: StockMoneyValue,
    low: StockMoneyValue,
) : SparkAdapter() {

  private val chartData: List<Chart.Data>
  private val baselineValue: Float

  init {
    val closes = chart.close()
    val baseline = chart.startingPrice()

    val data =
        chart.dates().mapIndexed { index, date ->
          val close = closes[index]
          return@mapIndexed Chart.Data(
              high = high,
              low = low,
              baseline = baseline,
              range = chart.range(),
              date = date,
              price = close,
          )
        }

    baselineValue = baseline.value().toFloat()
    chartData = data
  }

  @CheckResult
  private fun getValue(item: Chart.Data): Float {
    return item.price.value().toFloat()
  }

  override fun getCount(): Int {
    return chartData.size
  }

  override fun getItem(index: Int): Chart.Data {
    return chartData[index]
  }

  override fun getY(index: Int): Float {
    // Offset the Y based on the baseline value which is either previous close or day's open
    return getValue(getItem(index)) - baselineValue
  }

  override fun getBaseLine(): Float {
    // Baseline of 0 to show the chart dipping below zero
    return 0F
  }
}

@Preview
@Composable
private fun PreviewChart() {
  Surface {
    Chart(
        modifier = Modifier.width(320.dp).height(160.dp),
        chart = newTestChart("MSFT".asSymbol()),
    )
  }
}

interface Chart {

  data class Data
  internal constructor(
      val high: StockMoneyValue,
      val low: StockMoneyValue,
      val baseline: StockMoneyValue,
      val date: LocalDateTime,
      val price: StockMoneyValue,
      val range: StockChart.IntervalRange,
  )
}
