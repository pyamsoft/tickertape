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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.home.item.HomeChartItem
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
  HomeCharts(
      modifier = modifier,
      name = "USA Indexes",
      isLoading = state.isLoadingIndexes,
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
  HomeCharts(
      modifier = modifier,
      name = "Today's Top Gainers (USA)",
      isLoading = state.isLoadingGainers,
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
  HomeCharts(
      modifier = modifier,
      name = "Today's Top Losers (USA)",
      isLoading = state.isLoadingLosers,
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
  HomeCharts(
      modifier = modifier,
      name = "Today's Top Trending Stocks (USA)",
      isLoading = state.isLoadingTrending,
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
  HomeCharts(
      modifier = modifier,
      name = "Today's Most Shorted Stocks",
      isLoading = state.isLoadingMostShorted,
      tickers = state.mostShorted,
      error = state.mostShortedError,
      onChartClicked = onChartClicked,
  )
}

@Composable
private fun HomeCharts(
    modifier: Modifier = Modifier,
    name: String,
    isLoading: Boolean,
    tickers: List<Ticker>,
    error: Throwable?,
    onChartClicked: (Ticker) -> Unit,
) {
  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      Column {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            text = name,
            style =
                MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )

        Box(
            modifier = Modifier.fillMaxWidth().height(HomeScreenDefaults.CHART_HEIGHT_DP.dp),
        ) {
          ChartList(
              modifier = Modifier.matchParentSize(),
              tickers = tickers,
              onClick = onChartClicked,
          )

          Loading(
              isLoading = isLoading,
              modifier = Modifier.matchParentSize(),
          )
        }
      }
    } else {
      Error(
          modifier = Modifier.fillMaxWidth(),
          error = err,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun Loading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
      modifier = modifier,
      visible = isLoading,
  ) {
    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator() }
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
    itemsIndexed(
        items = onlyChartTickers,
        key = { _, item -> item.symbol.symbol() },
    ) { index, item ->
      // We can assume here the chart is not null
      HomeChartItem(
          modifier =
              Modifier.fillMaxHeight().width(HomeScreenDefaults.ITEM_WIDTH_DP.dp).run {
                when (index) {
                  0 -> padding(start = 16.dp)
                  onlyChartTickers.lastIndex -> padding(end = 16.dp)
                  else -> this
                }
              },
          ticker = item,
          onClick = onClick,
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
private fun PreviewHomeCharts() {
  val symbol = "MSFT".asSymbol()
  Surface {
    HomeCharts(
        tickers =
            listOf(
                Ticker(
                    symbol = symbol,
                    quote = newTestQuote(symbol),
                    chart = newTestChart(symbol),
                ),
            ),
        isLoading = false,
        name = "TEST STOCKS CHARTS",
        error = null,
        onChartClicked = {},
    )
  }
}
