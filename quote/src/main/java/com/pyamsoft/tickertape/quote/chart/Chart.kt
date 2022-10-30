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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDateTime

data class ChartData
internal constructor(
    val high: StockMoneyValue,
    val low: StockMoneyValue,
    val baseline: StockMoneyValue,
    val date: LocalDateTime,
    val price: StockMoneyValue,
    val range: StockChart.IntervalRange,
)

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    chart: StockChart,
    onScrub: ((ChartData) -> Unit)? = null,
) {
  Box(
      modifier = modifier,
  ) {
    LineChart(
        modifier = Modifier.matchParentSize().padding(start = MaterialTheme.keylines.baseline),
        chart = chart,
        onScrub = onScrub,
    )

    ChartBounds(
        modifier = Modifier.matchParentSize(),
        chart = chart,
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
