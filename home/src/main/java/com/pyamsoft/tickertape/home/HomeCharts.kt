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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.home.item.HomeChartItem
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.ChartError
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
internal fun HomeIndexes(
    modifier: Modifier = Modifier,
    state: HomeIndexesViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
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
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
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
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
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
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
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
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Most Shorted Stocks",
      isLoading = state.isLoadingMostShorted,
      tickers = state.mostShorted,
      error = state.mostShortedError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeMostActive(
    modifier: Modifier = Modifier,
    state: HomeMostActiveViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Most Active Stocks",
      isLoading = state.isLoadingMostActive,
      tickers = state.mostActive,
      error = state.mostActiveError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeGrowthTech(
    modifier: Modifier = Modifier,
    state: HomeGrowthTechViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Growth Tech Stocks",
      isLoading = state.isLoadingGrowthTech,
      tickers = state.growthTech,
      error = state.growthTechError,
      onChartClicked = onChartClicked,
  )
}

@Composable
internal fun HomeUndervaluedGrowth(
    modifier: Modifier = Modifier,
    state: HomeUndervaluedGrowthViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Undervalued Growth Stocks",
      isLoading = state.isLoadingUndervaluedGrowth,
      tickers = state.undervaluedGrowth,
      error = state.undervaluedGrowthError,
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
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
) {
  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      Column {
        Text(
            modifier =
                Modifier.padding(
                    start = MaterialTheme.keylines.content,
                    bottom = MaterialTheme.keylines.baseline,
                ),
            text = name,
            style =
                MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )

        Box(
            modifier = Modifier.fillMaxWidth().height(HomeScreenDefaults.CHART_HEIGHT_DP),
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
      ChartError(
          modifier = Modifier.fillMaxWidth(),
          error = err,
          imageLoader = imageLoader,
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
        modifier = Modifier.padding(MaterialTheme.keylines.content),
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
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.baseline),
  ) {
    itemsIndexed(
        items = onlyChartTickers,
        key = { _, item -> item.symbol.symbol() },
    ) { index, item ->
      // We can assume here the chart is not null
      HomeChartItem(
          modifier =
              Modifier.fillMaxHeight().width(HomeScreenDefaults.ITEM_WIDTH_DP).run {
                when (index) {
                  0 -> padding(start = MaterialTheme.keylines.content)
                  onlyChartTickers.lastIndex -> padding(end = MaterialTheme.keylines.content)
                  else -> this
                }
              },
          ticker = item,
          onClick = onClick,
      )
    }
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
        imageLoader = createNewTestImageLoader(),
    )
  }
}
