/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.tickertape.home.item.HomeChartItem
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.chart.ChartError
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun HomeIndexes(
    modifier: Modifier = Modifier,
    state: HomeIndexesViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingIndexes.collectAsStateWithLifecycle()
  val error by state.indexesError.collectAsStateWithLifecycle()
  val tickers = state.indexes.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "USA Indexes",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeGainers(
    modifier: Modifier = Modifier,
    state: HomeGainersViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingGainers.collectAsStateWithLifecycle()
  val error by state.gainersError.collectAsStateWithLifecycle()
  val tickers = state.gainers.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Top Gainers (USA)",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeLosers(
    modifier: Modifier = Modifier,
    state: HomeLosersViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingLosers.collectAsStateWithLifecycle()
  val error by state.losersError.collectAsStateWithLifecycle()
  val tickers = state.losers.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Top Losers (USA)",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeTrending(
    modifier: Modifier = Modifier,
    state: HomeTrendingViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingTrending.collectAsStateWithLifecycle()
  val error by state.trendingError.collectAsStateWithLifecycle()
  val tickers = state.trending.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Top Trending Stocks (USA)",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeMostShorted(
    modifier: Modifier = Modifier,
    state: HomeShortedViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingMostShorted.collectAsStateWithLifecycle()
  val error by state.mostShortedError.collectAsStateWithLifecycle()
  val tickers = state.mostShorted.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Most Shorted Stocks",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeMostActive(
    modifier: Modifier = Modifier,
    state: HomeMostActiveViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingMostActive.collectAsStateWithLifecycle()
  val error by state.mostActiveError.collectAsStateWithLifecycle()
  val tickers = state.mostActive.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Today's Most Active Stocks",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeGrowthTech(
    modifier: Modifier = Modifier,
    state: HomeGrowthTechViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingGrowthTech.collectAsStateWithLifecycle()
  val error by state.growthTechError.collectAsStateWithLifecycle()
  val tickers = state.growthTech.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Growth Tech Stocks",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
internal fun HomeUndervaluedGrowth(
    modifier: Modifier = Modifier,
    state: HomeUndervaluedGrowthViewState,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingUndervaluedGrowth.collectAsStateWithLifecycle()
  val error by state.undervaluedGrowthError.collectAsStateWithLifecycle()
  val tickers = state.undervaluedGrowth.collectAsStateListWithLifecycle()

  val isLoading = remember(loadingState) { loadingState != HomeBaseViewState.LoadingState.DONE }

  HomeCharts(
      modifier = modifier,
      imageLoader = imageLoader,
      name = "Undervalued Growth Stocks",
      isLoading = isLoading,
      tickers = tickers,
      error = error,
      onChartClicked = onChartClicked,
      onRefresh = onRefresh,
  )
}

@Composable
private fun HomeCharts(
    modifier: Modifier = Modifier,
    name: String,
    isLoading: Boolean,
    tickers: SnapshotStateList<HomeStock>,
    error: Throwable?,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  FirstRenderEffect { onRefresh() }

  Crossfade(
      label = "Chart",
      targetState = error,
  ) { err ->
    if (err == null) {
      Column(
          modifier = modifier,
      ) {
        Text(
            modifier =
                Modifier.padding(
                    start = MaterialTheme.keylines.content,
                    bottom = MaterialTheme.keylines.baseline,
                ),
            text = name,
            style =
                MaterialTheme.typography.body1.copy(
                    fontWeight = FontWeight.W400,
                ),
        )

        Box(
            modifier = Modifier.fillMaxWidth().height(HomeScreenDefaults.rememberChartHeight()),
        ) {
          ChartList(
              modifier = Modifier.matchParentSize(),
              tickers = tickers,
              onClick = onChartClicked,
          )

          Loading(
              modifier = Modifier.matchParentSize(),
              isLoading = isLoading,
          )
        }
      }
    } else {
      ChartError(
          modifier = modifier.fillMaxWidth(),
          error = err,
          imageLoader = imageLoader,
      )
    }
  }
}

@Composable
private fun Loading(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
) {
  AnimatedVisibility(
      modifier = modifier,
      visible = isLoading,
      enter = fadeIn(),
      exit = fadeOut(),
  ) {
    Box(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator()
    }
  }
}

@Composable
private fun ChartList(
    modifier: Modifier = Modifier,
    tickers: SnapshotStateList<HomeStock>,
    onClick: (Ticker) -> Unit,
) {
  val onlyChartTickers = remember(tickers) { tickers.filter { it.chart != null } }

  LazyRow(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
  ) {
    itemsIndexed(
        items = onlyChartTickers,
        key = { index, item -> "${item.ticker.symbol.raw}-${index}" },
    ) { index, item ->
      // We can assume here the chart is not null
      HomeChartItem(
          modifier =
              Modifier.fillMaxHeight().width(HomeScreenDefaults.rememberItemWidth()).run {
                when (index) {
                  0 -> padding(start = MaterialTheme.keylines.content)
                  onlyChartTickers.lastIndex -> padding(end = MaterialTheme.keylines.content)
                  else -> this
                }
              },
          onClick = onClick,
          ticker = item.ticker,
          // Safe to assume
          painter = item.chart.requireNotNull(),
      )
    }
  }
}

@Preview
@Composable
private fun PreviewHomeCharts() {
  HomeCharts(
      tickers = remember { mutableStateListOf() },
      imageLoader = createNewTestImageLoader(),
      isLoading = false,
      name = "TEST STOCKS CHARTS",
      error = null,
      onChartClicked = {},
      onRefresh = {},
  )
}
