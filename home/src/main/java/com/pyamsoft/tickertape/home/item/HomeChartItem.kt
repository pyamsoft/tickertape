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

package com.pyamsoft.tickertape.home.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerName
import com.pyamsoft.tickertape.quote.TickerPrice
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun HomeChartItem(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onClick: (Ticker) -> Unit,
) {
  // We can assume here the chart is not null
  Column(
      modifier = modifier.clickable { onClick(ticker) },
  ) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
      TickerName(
          modifier = Modifier.weight(1F),
          ticker = ticker,
      )
      TickerPrice(
          modifier = Modifier.padding(start = 16.dp),
          ticker = ticker,
      )
    }
    Chart(
        chart = ticker.chart.requireNotNull(),
    )
  }
}

@Preview
@Composable
private fun PreviewHomeChartItem() {
  val symbol = "MSFT".asSymbol()
  Surface {
    HomeChartItem(
        ticker =
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = newTestChart(symbol),
            ),
        onClick = {},
    )
  }
}