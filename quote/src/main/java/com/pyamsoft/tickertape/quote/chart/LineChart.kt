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

import android.graphics.Color
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.entryModelOf
import com.patrykandpatryk.vico.core.entry.entryOf
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.rememberInBackground

private data class ChartLines(
    val models: ChartEntryModel,
    val specs: List<LineChart.LineSpec>,
)

@CheckResult
@Composable
private fun rememberChartLines(chart: StockChart): ChartLines? {
  return rememberInBackground(chart) {
    val models =
        entryModelOf(
            listOf(
                entryOf(0, 0),
                entryOf(1, 1),
                entryOf(2, 2),
                entryOf(3, 4),
                entryOf(4, 2),
                entryOf(4.5, 0.5),
            ),
            listOf(
                entryOf(4.5, 0.5),
                entryOf(5, -1),
                entryOf(6, -2),
                entryOf(6.5, 0),
            ),
            listOf(
                entryOf(6.5, 0),
                entryOf(7, 2),
                entryOf(8, 2),
                entryOf(8.4, 0),
            ),
            listOf(
                entryOf(8.4, 0),
                entryOf(9, -3),
                entryOf(10, 0),
            ),
        )

    val specs =
        listOf(
            LineChart.LineSpec(lineColor = Color.GREEN),
            LineChart.LineSpec(lineColor = Color.RED),
            LineChart.LineSpec(lineColor = Color.GREEN),
            LineChart.LineSpec(lineColor = Color.RED),
        )

    return@rememberInBackground ChartLines(
        models = models,
        specs = specs,
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
      Chart(
          modifier = Modifier.fillMaxSize(),
          chart =
              lineChart(
                  lines = lines.specs,
              ),
          model = lines.models,
          startAxis =
              startAxis(
                  // No labels on start axis
                  valueFormatter = { _, _ -> "" },
              ),
          bottomAxis =
              bottomAxis(
                  // No labels on bottom axis
                  valueFormatter = { _, _ -> "" },
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
