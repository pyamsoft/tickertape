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
import com.patrykandpatryk.vico.core.chart.line.LineChart as VicoLineChart
import com.patrykandpatryk.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatryk.vico.core.dimensions.MutableDimensions
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.FloatEntry
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.HairlineSize
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import kotlin.math.roundToInt

@Stable
private data class ChartLines(
    val models: ChartEntryModel,
    val specs: SnapshotStateList<VicoLineChart.LineSpec>,
)

/** Can't be data class */
@Stable
private class ChartDataEntry(
    private val data: ChartDataCoordinates,
) :
    ChartEntry by FloatEntry(
        x = data.x,
        y = data.y,
    ) {

  override fun toString(): String {
    return "ChartDataEntry(x=$x, y=$y)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ChartDataEntry

    if (data != other.data) return false

    return true
  }

  override fun hashCode(): Int {
    return data.hashCode()
  }
}

@Composable
@CheckResult
private fun rememberLineDecorations(
    startingPrice: StockMoneyValue,
): List<Decoration> {
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
          startingPrice,
          lineShape,
          lineText,
      ) {
        ThresholdLine(
            thresholdValue = if (startingPrice.isValid) startingPrice.value.toFloat() else 0F,
            lineComponent = lineShape,
            labelComponent = lineText,
        )
      }

  return remember(baselineDecoration) { listOf(baselineDecoration) }
}

@CheckResult
@Composable
private fun rememberChartLines(
    painter: ChartDataPainter,
): ChartLines {
  return remember(painter) {
    val producer =
        ChartEntryModelProducer(
            painter.coordinates.map { ChartDataEntry(it) },
        )

    val spec =
        VicoLineChart.LineSpec(
            lineColor = painter.color,
            lineBackgroundShader =
                verticalGradient(
                    colors =
                        Color(painter.color).let { c ->
                          arrayOf(
                              c.copy(alpha = 0.7F),
                              c.copy(alpha = 0.3F),
                          )
                        },
                ),
        )

    return@remember ChartLines(
        models = producer.getModel(),
        specs = mutableStateListOf(spec),
    )
  }
}

@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    painter: ChartDataPainter,
    onScrub: ((ChartData) -> Unit)? = null,
) {
  val startingMoney = painter.startingMoney
  val lines = rememberChartLines(painter)
  val decorations = rememberLineDecorations(startingMoney)

  val axisValuesOverrider =
      remember(
          lines.models,
          startingMoney,
      ) {
        // Adjust the Y axis to include the baseline starting price if needed
        val baseline = if (startingMoney.isValid) startingMoney.value.toFloat() else 0F
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

  val scrollSpec =
      rememberChartScrollSpec<ChartEntryModel>(
          isScrollEnabled = false,
      )

  Chart(
      modifier = modifier.fillMaxSize().padding(start = MaterialTheme.keylines.baseline),
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
      chartScrollSpec = scrollSpec,
  )
}

@Preview
@Composable
private fun PreviewLineChart() {
  LineChart(
      modifier = Modifier.width(320.dp).height(160.dp),
      painter = ChartDataPainter.EMPTY,
  )
}
