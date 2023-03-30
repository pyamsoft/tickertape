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

package com.pyamsoft.tickertape.portfolio.dig.chart

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.chart.ChartData
import com.pyamsoft.tickertape.quote.dig.ChartDigViewState
import com.pyamsoft.tickertape.quote.dig.chart.DigChart
import com.pyamsoft.tickertape.quote.test.newTestDigViewState
import com.pyamsoft.tickertape.stocks.api.StockChart

@Composable
internal fun PortfolioChart(
    modifier: Modifier = Modifier,
    state: ChartDigViewState,
    onScrub: (ChartData) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  DigChart(
      modifier = modifier,
      state = state,
      onScrub = onScrub,
      onRangeSelected = onRangeSelected,
  )
}

@Preview
@Composable
private fun PreviewPortfolioChart() {
  Surface {
    PortfolioChart(
        state = newTestDigViewState(),
        onScrub = {},
        onRangeSelected = {},
    )
  }
}
