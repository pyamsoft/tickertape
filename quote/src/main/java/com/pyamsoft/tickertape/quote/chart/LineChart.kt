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

package com.pyamsoft.tickertape.quote.chart

import androidx.annotation.CheckResult
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatryk.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.core.isNegative
import com.pyamsoft.tickertape.core.isPositive
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.periodHigh
import com.pyamsoft.tickertape.stocks.api.periodLow
import com.pyamsoft.tickertape.ui.rememberInBackground
import kotlin.math.roundToInt

private data class ChartLines(
    val models: ChartEntryModel,
    val specs: List<LineChart.LineSpec>,
)

@CheckResult
private fun ChartData.priceValueAdjustedToBaseline(): Double {
  return this.price.value - this.baseline.value
}

private class ChartDataEntry(
    private val data: ChartData?,
    x: Float,
    y: Float,
) :
    ChartEntry by FloatEntry(
        x = x,
        y = y,
    ) {

  @CheckResult
  fun isDataPoint(): Boolean {
    return data != null
  }

  @CheckResult
  fun getData(): ChartData {
    return data.requireNotNull { "Cannot get ChartData from empty data point" }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ChartDataEntry

    if (data != other.data) return false
    if (x != other.x) return false
    if (y != other.y) return false
    return true
  }

  override fun toString(): String {
    return if (isDataPoint()) {
      "ChartDataEntry(x=$x, y=$y)"
    } else {
      "ChartDataEntry(Fake)"
    }
  }

  override fun hashCode(): Int {
    var result = data?.hashCode() ?: 0
    result = 31 * result + x.hashCode()
    result = 31 * result + y.hashCode()
    return result
  }
}

@CheckResult
private fun ChartData.resolvePriceDirection(): StockDirection {
  val adjusted = this.priceValueAdjustedToBaseline()
  // Decide, based on the baseline value, where the direction is going
  return when {
    adjusted.isPositive() -> StockDirection.UP
    adjusted.isNegative() -> StockDirection.DOWN
    else -> StockDirection.NONE
  }
}

private fun finishSegments(
    lineSegments: MutableList<List<ChartDataEntry>>,
    specSegments: MutableList<LineChart.LineSpec>,
    currentSegment: List<ChartDataEntry>,
    currentDirection: StockDirection,
) {
  // We are done with this segment, add it to the list of segments
  lineSegments.add(currentSegment)
  // Derive a color for the segment
  specSegments.add(
      LineChart.LineSpec(
          lineColor =
              when {
                currentDirection.isUp -> DEFAULT_STOCK_UP_COLOR
                currentDirection.isDown -> DEFAULT_STOCK_DOWN_COLOR
                else -> DEFAULT_STOCK_COLOR
              },
      ),
  )
}

@Composable
@CheckResult
private fun rememberChartLines(chart: StockChart): ChartLines? {
  return rememberInBackground(chart) {
    val high = chart.periodHigh()
    val low = chart.periodLow()
    val range = chart.range

    val closes = chart.close
    val baseline = chart.startingPrice

    val dataPoints =
        chart.dates.mapIndexed { i, date ->
          val close = closes[i]
          ChartData(
              high = high,
              low = low,
              baseline = baseline,
              range = range,
              date = date,
              price = close,
          )
        }

    val specSegments = mutableListOf<LineChart.LineSpec>()
    val lineSegments = mutableListOf<List<ChartDataEntry>>()

    var index = 0
    var lastData: ChartData? = null
    val currentSegment = mutableListOf<ChartDataEntry>()

    while (true) {
      // If we have no more points, we are done
      val data = dataPoints.getOrNull(index) ?: break

      val x = index.toFloat()

      // Grab a data point and chart it
      currentSegment.add(
          ChartDataEntry(
              data = data,
              x = x,
              y = data.price.value.toFloat(),
          ),
      )

      // Bump the index
      lastData = data
      ++index
    }

    val lineColor =
        if (lastData == null) DEFAULT_STOCK_COLOR
        else {
          val price = lastData.price.value
          val base = lastData.baseline.value
          when {
            price < base -> DEFAULT_STOCK_DOWN_COLOR
            price > base -> DEFAULT_STOCK_UP_COLOR
            else -> DEFAULT_STOCK_COLOR
          }
        }

    val fillColor = Color(lineColor)

    // Only one segment, color the chart based on the final price compared to the baseline
    lineSegments.add(currentSegment)
    specSegments.add(
        LineChart.LineSpec(
            lineColor = lineColor,
            lineBackgroundShader =
                verticalGradient(
                    colors =
                        arrayOf(
                            fillColor.copy(alpha = 0.7F),
                            fillColor.copy(alpha = 0.3F),
                        ),
                ),
        ),
    )

    return@rememberInBackground ChartLines(
        models = ChartEntryModelProducer(lineSegments).getModel(),
        specs = specSegments,
    )
  }
}

@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    chart: StockChart,
    onScrub: ((ChartData) -> Unit)? = null,
) {
  val chartLines = rememberChartLines(chart)

  Crossfade(
      modifier = modifier,
      targetState = chartLines,
  ) { lines ->
    if (lines == null) {
      Box(
          modifier = Modifier.fillMaxSize().padding(MaterialTheme.keylines.content),
          contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
    } else {
      val axisValuesOverrider =
          remember(lines.models) {
            AxisValuesOverrider.fixed(
                minX = lines.models.minX,
                maxX = lines.models.maxX,
                minY = lines.models.minY,
                maxY = lines.models.maxY,
            )
          }

      Chart(
          modifier = Modifier.fillMaxSize(),
          isZoomEnabled = false,
          chart =
              lineChart(
                  lines = lines.specs,
                  axisValuesOverrider = axisValuesOverrider,
              ),
          model = lines.models,
          startAxis =
              startAxis(
                  // No ticks
                  tick = null,
                  // No labels on start axis
                  valueFormatter = { _, _ -> "" },
              ),
          bottomAxis =
              bottomAxis(
                  // No ticks
                  tick = null,
                  // No labels on bottom axis
                  valueFormatter = { _, _ -> "" },
                  tickPosition =
                      HorizontalAxis.TickPosition.Center(
                          // Spacing so large there will not be any ticks
                          // Offset by 1 to avoid a crash where Offset cannot be less than 1
                          spacing = lines.models.maxX.roundToInt() * 2 + 1,
                      ),
              ),
          chartScrollSpec =
              rememberChartScrollSpec(
                  isScrollEnabled = false,
              ),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewLineChart() {
  Surface {
    LineChart(
        modifier = Modifier.width(320.dp).height(160.dp),
        chart = newTestChart("MSFT".asSymbol()),
    )
  }
}
