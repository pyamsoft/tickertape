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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart as ComposeChart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.entry.entryModelOf
import com.patrykandpatryk.vico.core.entry.entryOf
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDateTime

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

@Composable
@JvmOverloads
fun Chart(
    modifier: Modifier = Modifier,
    chart: StockChart,
    onScrub: ((Chart.Data?) -> Unit)? = null,
) {
  val chartModel = remember {
    entryModelOf(
        listOf(
            entryOf(0, 0),
            entryOf(1, 1),
            entryOf(2, 2),
            entryOf(3, 4),
            entryOf(4, 2),
        ),
        listOf(
            entryOf(5, -1),
            entryOf(6, -2),
        ),
        listOf(
            entryOf(7, 2),
            entryOf(8, 2),
        ),
        listOf(
            entryOf(9, -3),
            entryOf(10, 0),
        ),
    )
  }

  val lineChartSpecs = remember {
    listOf(
        LineChart.LineSpec(lineColor = Color.GREEN),
        LineChart.LineSpec(lineColor = Color.RED),
        LineChart.LineSpec(lineColor = Color.GREEN),
        LineChart.LineSpec(lineColor = Color.RED),
    )
  }

  Box(
      modifier = modifier,
  ) {
    ComposeChart(
        modifier = Modifier.matchParentSize(),
        chart =
            lineChart(
                lines = lineChartSpecs,
            ),
        model = chartModel,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
    )
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
