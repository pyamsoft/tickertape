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

package com.pyamsoft.tickertape.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun HomeIndexes(
    modifier: Modifier = Modifier,
    state: HomeIndexesViewState,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeChartItem(
      modifier = modifier,
      tickers = state.indexes,
      error = state.indexesError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeGainers(
    modifier: Modifier = Modifier,
    state: HomeGainersViewState,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeChartItem(
      modifier = modifier,
      tickers = state.gainers,
      error = state.gainersError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeLosers(
    modifier: Modifier = Modifier,
    state: HomeLosersViewState,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeChartItem(
      modifier = modifier,
      tickers = state.losers,
      error = state.losersError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeTrending(
    modifier: Modifier = Modifier,
    state: HomeTrendingViewState,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeChartItem(
      modifier = modifier,
      tickers = state.trending,
      error = state.trendingError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeMostShorted(
    modifier: Modifier = Modifier,
    state: HomeShortedViewState,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeChartItem(
      modifier = modifier,
      tickers = state.mostShorted,
      error = state.mostShortedError,
      onChartClicked = onChartClicked,
  )
}

@Composable
private fun HomeChartItem(
    modifier: Modifier = Modifier,
    tickers: List<Ticker>,
    error: Throwable?,
    onChartClicked: (Ticker) -> Unit,
) {
  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      ChartList(
          modifier = Modifier.fillMaxWidth(),
          tickers = tickers,
          onClick = onChartClicked,
      )
    } else {
      Error(
          modifier = Modifier.fillMaxWidth(),
          error = err,
      )
    }
  }
}

@Composable
private fun ChartList(
    modifier: Modifier = Modifier,
    tickers: List<Ticker>,
    onClick: (Ticker) -> Unit,
) {
  val onlyChartTickers = remember(tickers) { tickers.filter { it.chart != null } }
  LazyRow(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(
        items = onlyChartTickers,
        key = { it.symbol.symbol() },
    ) { item ->
      // We can assume here the chart is not null
      Chart(
          modifier = Modifier.clickable { onClick(item) }.height(160.dp).width(320.dp),
          chart = item.chart.requireNotNull(),
      )
    }
  }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier,
    error: Throwable,
) {
  Column(
      modifier = modifier.padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        textAlign = TextAlign.Center,
        text = error.message ?: "An unexpected error occurred",
        style =
            MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.error,
            ),
    )

    Text(
        modifier = Modifier.padding(top = 16.dp),
        textAlign = TextAlign.Center,
        text = "Please try again later.",
        style = MaterialTheme.typography.body2,
    )
  }
}

@Preview
@Composable
private fun PreviewHomeChartItem() {
  val symbol = "MSFT".asSymbol()
  Surface {
    HomeChartItem(
        tickers =
            listOf(
                Ticker(
                    symbol = symbol,
                    quote = newTestQuote(symbol),
                    chart = newTestChart(symbol),
                ),
            ),
        error = null,
        onChartClicked = {},
    )
  }
}