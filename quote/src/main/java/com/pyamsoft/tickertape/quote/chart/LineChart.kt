/*
 * Copyright 2023 pyamsoft
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.patrykandpatryk.vico.compose.component.shapeComponent
import com.patrykandpatryk.vico.compose.component.textComponent
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.chart.decoration.Decoration
import com.patrykandpatryk.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatryk.vico.core.dimensions.MutableDimensions
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.pyamsoft.pydroid.theme.HairlineSize
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.periodHigh
import com.pyamsoft.tickertape.stocks.api.periodLow
import com.pyamsoft.tickertape.ui.rememberInBackground
import com.pyamsoft.tickertape.ui.test.TestClock
import kotlin.math.roundToInt

@Stable
private data class ChartLines(
    val models: ChartEntryModel,
    val specs: SnapshotStateList<LineChart.LineSpec>,
)

/** Can't be data class */
@Stable
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

    // Only one segment, color the chart based on the final price compared to the baseline
    val lineColor =
        dataPoints.lastOrNull().let { lastData ->
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
        }

    return@rememberInBackground ChartLines(
        models =
            ChartEntryModelProducer(
                    dataPoints.mapIndexed { i, d ->
                      ChartDataEntry(
                          data = d,
                          x = i.toFloat(),
                          y = d.price.value.toFloat(),
                      )
                    },
                )
                .getModel(),
        specs =
            mutableStateListOf(
                LineChart.LineSpec(
                    lineColor = lineColor,
                    lineBackgroundShader =
                        verticalGradient(
                            colors =
                                Color(lineColor).let { c ->
                                  arrayOf(
                                      c.copy(alpha = 0.7F),
                                      c.copy(alpha = 0.3F),
                                  )
                                },
                        ),
                ),
            ),
    )
  }
}

@Composable
@CheckResult
private fun rememberLineDecorations(chart: StockChart): List<Decoration> {
  val lineShape =
      shapeComponent(
          color = MaterialTheme.colors.secondary,
          strokeWidth = HairlineSize,
          strokeColor = MaterialTheme.colors.secondary,
      )
  val lineText =
      textComponent(
          color = MaterialTheme.colors.onSecondary,
          textSize = MaterialTheme.typography.overline.fontSize,
          margins =
              MutableDimensions(
                  horizontalDp = MaterialTheme.keylines.typography.value / 2,
                  verticalDp = MaterialTheme.keylines.typography.value,
              ),
          padding =
              MutableDimensions(
                  horizontalDp = MaterialTheme.keylines.typography.value,
                  verticalDp = 0F,
              ),
          background =
              shapeComponent(
                  color = MaterialTheme.colors.secondary,
                  shape = MaterialTheme.shapes.small,
              ),
      )

  val baselineDecoration =
      remember(
          chart.startingPrice,
          lineShape,
          lineText,
      ) {
        ThresholdLine(
            thresholdValue = chart.startingPrice.value.toFloat(),
            lineComponent = lineShape,
            labelComponent = lineText,
        )
      }

  return remember(baselineDecoration) { listOf(baselineDecoration) }
}

@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    chart: StockChart,
    onScrub: ((ChartData) -> Unit)? = null,
) {
  val lines = rememberChartLines(chart)

  val decorations = rememberLineDecorations(chart)

  AnimatedVisibility(
      modifier = modifier,
      visible = lines != null,
      enter = fadeIn(),
      exit = fadeOut(),
  ) {
    if (lines != null) {
      val axisValuesOverrider =
          remember(
              lines.models,
              chart.startingPrice,
          ) {
            // Adjust the Y axis to include the baseline starting price if needed
            val baseline = chart.startingPrice.value.toFloat()
            val models = lines.models
            val adjustedMinY = if (baseline < models.minY) baseline else models.minY
            val adjustedMaxY = if (baseline > models.maxY) baseline else models.maxY
            return@remember AxisValuesOverrider.fixed(
                minX = lines.models.minX,
                maxX = lines.models.maxX,
                minY = adjustedMinY,
                maxY = adjustedMaxY,
            )
          }

      Chart(
          modifier = Modifier.fillMaxSize().padding(start = MaterialTheme.keylines.baseline),
          isZoomEnabled = false,
          chart =
              lineChart(
                  lines = lines.specs,
                  axisValuesOverrider = axisValuesOverrider,
                  decorations = decorations,
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
  val symbol = TestSymbol
  val clock = TestClock

  LineChart(
      modifier = Modifier.width(320.dp).height(160.dp),
      chart = newTestChart(symbol, clock),
  )
}
