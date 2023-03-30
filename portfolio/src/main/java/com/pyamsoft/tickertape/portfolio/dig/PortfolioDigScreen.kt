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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.chart.PortfolioChart
import com.pyamsoft.tickertape.portfolio.dig.position.PositionScreen
import com.pyamsoft.tickertape.portfolio.dig.splits.SplitScreen
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.chart.ChartData
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.news.DigNews
import com.pyamsoft.tickertape.quote.dig.options.DigOptionsChain
import com.pyamsoft.tickertape.quote.dig.pricealert.DigPriceAlerts
import com.pyamsoft.tickertape.quote.dig.recommend.DigRecommendations
import com.pyamsoft.tickertape.quote.dig.statistics.DigKeyStatistics
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.ui.test.TestClock
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import kotlinx.coroutines.flow.collectLatest
import java.time.Clock
import java.time.LocalDate

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PortfolioDigScreen(
    modifier: Modifier = Modifier,
    clock: Clock,
    state: PortfolioDigViewState,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
    onAddNewHolding: () -> Unit,
    // Chart
    onChartScrub: (ChartData) -> Unit,
    onChartRangeSelected: (StockChart.IntervalRange) -> Unit,
    // Positions
    onPositionAdd: (DbHolding) -> Unit,
    onPositionUpdate: (DbPosition, DbHolding) -> Unit,
    onPositionDelete: (DbPosition) -> Unit,
    onPositionDeleteFinalized: () -> Unit,
    onPositionRestored: () -> Unit,
    // Splits
    onSplitAdd: (DbHolding) -> Unit,
    onSplitUpdated: (DbSplit, DbHolding) -> Unit,
    onSplitDeleted: (DbSplit) -> Unit,
    onSplitDeleteFinalized: () -> Unit,
    onSplitRestored: () -> Unit,
    // Recommendations
    onRecClick: (Ticker) -> Unit,
    // Options
    onOptionSectionChanged: (StockOptions.Contract.Type) -> Unit,
    onOptionExpirationDateChanged: (LocalDate) -> Unit,
    // Price Alerts
    onAddPriceAlert: () -> Unit,
    onUpdatePriceAlert: (PriceAlert) -> Unit,
    onDeletePriceAlert: (PriceAlert) -> Unit,
) {
  val loadingState by state.loadingState.collectAsState()
  val ticker by state.ticker.collectAsState()
  val holding by state.holding.collectAsState()

  val allTabs = rememberTabs(ticker.symbol, holding)
  val pagerState = rememberPagerState()

  // Watch for a swipe causing a page change and update accordingly
  val handleTabUpdated by rememberUpdatedState(onTabUpdated)
  LaunchedEffect(
      pagerState,
      allTabs,
  ) {
    snapshotFlow { pagerState.currentPage }
        .collectLatest { index ->
          if (allTabs.isNotEmpty()) {
            val page = allTabs[index]
            handleTabUpdated(page)
          }
        }
  }

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      PortfolioDigToolbar(
          // Z-Index to place it above the SwipeRefresh indicator
          modifier = Modifier.fillMaxWidth().zIndex(1F),
          state = state,
          pagerState = pagerState,
          allTabs = allTabs,
          onClose = onClose,
      )

      Crossfade(
          modifier = Modifier.fillMaxWidth().weight(1F),
          targetState = loadingState,
      ) { loading ->
        when (loading) {
          BaseDigViewState.LoadingState.NONE,
          BaseDigViewState.LoadingState.LOADING -> {
            Loading(
                modifier = Modifier.fillMaxSize(),
            )
          }
          BaseDigViewState.LoadingState.DONE -> {
            Content(
                modifier = Modifier.fillMaxSize(),
                clock = clock,
                state = state,
                imageLoader = imageLoader,
                pagerState = pagerState,
                allTabs = allTabs,
                onChartScrub = onChartScrub,
                onChartRangeSelected = onChartRangeSelected,
                onRefresh = onRefresh,
                onPositionAdd = onPositionAdd,
                onPositionDelete = onPositionDelete,
                onPositionUpdate = onPositionUpdate,
                onPositionRestored = onPositionRestored,
                onPositionDeleteFinalized = onPositionDeleteFinalized,
                onSplitAdd = onSplitAdd,
                onSplitDeleted = onSplitDeleted,
                onSplitUpdated = onSplitUpdated,
                onSplitRestored = onSplitRestored,
                onSplitDeleteFinalized = onSplitDeleteFinalized,
                onRecClick = onRecClick,
                onOptionSectionChanged = onOptionSectionChanged,
                onOptionExpirationDateChanged = onOptionExpirationDateChanged,
                onAddPriceAlert = onAddPriceAlert,
                onUpdatePriceAlert = onUpdatePriceAlert,
                onDeletePriceAlert = onDeletePriceAlert,
                onAddNewHolding = onAddNewHolding,
            )
          }
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Content(
    modifier: Modifier = Modifier,
    clock: Clock,
    state: PortfolioDigViewState,
    imageLoader: ImageLoader,
    pagerState: PagerState,
    allTabs: SnapshotStateList<PortfolioDigSections>,
    onRefresh: () -> Unit,
    onAddNewHolding: () -> Unit,

    // Chart
    onChartScrub: (ChartData) -> Unit,
    onChartRangeSelected: (StockChart.IntervalRange) -> Unit,
    // Positions
    onPositionAdd: (DbHolding) -> Unit,
    onPositionUpdate: (DbPosition, DbHolding) -> Unit,
    onPositionDelete: (DbPosition) -> Unit,
    onPositionDeleteFinalized: () -> Unit,
    onPositionRestored: () -> Unit,
    // Splits
    onSplitAdd: (DbHolding) -> Unit,
    onSplitUpdated: (DbSplit, DbHolding) -> Unit,
    onSplitDeleted: (DbSplit) -> Unit,
    onSplitDeleteFinalized: () -> Unit,
    onSplitRestored: () -> Unit,
    // Recommendations
    onRecClick: (Ticker) -> Unit,
    // Options
    onOptionSectionChanged: (StockOptions.Contract.Type) -> Unit,
    onOptionExpirationDateChanged: (LocalDate) -> Unit,
    // Price Alerts
    onAddPriceAlert: () -> Unit,
    onUpdatePriceAlert: (PriceAlert) -> Unit,
    onDeletePriceAlert: (PriceAlert) -> Unit,
) {
  HorizontalPager(
      modifier = modifier,
      pageCount = allTabs.size,
      state = pagerState,
  ) { page ->
    val section =
        remember(
            page,
            allTabs,
        ) {
          allTabs[page]
        }
    when (section) {
      PortfolioDigSections.CHART -> {
        PortfolioChart(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onScrub = onChartScrub,
            onRangeSelected = onChartRangeSelected,
        )
      }
      PortfolioDigSections.NEWS -> {
        DigNews(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onRefresh = onRefresh,
        )
      }
      PortfolioDigSections.PRICE_ALERTS -> {
        DigPriceAlerts(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onAddPriceAlert = onAddPriceAlert,
            onUpdatePriceAlert = onUpdatePriceAlert,
            onDeletePriceAlert = onDeletePriceAlert,
        )
      }
      PortfolioDigSections.POSITIONS -> {
        PositionScreen(
            modifier = Modifier.fillMaxSize(),
            clock = clock,
            state = state,
            onRefresh = onRefresh,
            onAddPosition = onPositionAdd,
            onDeletePosition = onPositionDelete,
            onUpdatePosition = onPositionUpdate,
            onPositionRestored = onPositionRestored,
            onPositionDeleteFinalized = onPositionDeleteFinalized,
            onAddNewHolding = onAddNewHolding,
        )
      }
      PortfolioDigSections.STATISTICS -> {
        DigKeyStatistics(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
        )
      }
      PortfolioDigSections.SPLITS -> {
        SplitScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onAddSplit = onSplitAdd,
            onDeleteSplit = onSplitDeleted,
            onUpdateSplit = onSplitUpdated,
            onSplitRestored = onSplitRestored,
            onSplitDeleteFinalized = onSplitDeleteFinalized,
            onAddNewHolding = onAddNewHolding,
        )
      }
      PortfolioDigSections.RECOMMENDATIONS -> {
        DigRecommendations(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onRecClick = onRecClick,
        )
      }
      PortfolioDigSections.OPTIONS_CHAIN -> {
        DigOptionsChain(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onSectionChanged = onOptionSectionChanged,
            onExpirationDateChanged = onOptionExpirationDateChanged,
        )
      }
    }
  }
}

@Composable
private fun Loading(
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator()
  }
}

@Preview
@Composable
private fun PreviewPortfolioDigScreen() {
  val symbol = TestSymbol
  val clock = TestClock

  PortfolioDigScreen(
      clock = clock,
      state =
          MutablePortfolioDigViewState(
              params =
                  PortfolioDigParams(
                      symbol = symbol,
                      equityType = EquityType.STOCK,
                      lookupSymbol = null,
                  ),
              clock = clock,
          ),
      imageLoader = createNewTestImageLoader(),
      onClose = {},
      onChartScrub = {},
      onChartRangeSelected = {},
      onTabUpdated = {},
      onRefresh = {},
      onPositionAdd = {},
      onPositionDelete = {},
      onPositionUpdate = { _, _ -> },
      onPositionDeleteFinalized = {},
      onPositionRestored = {},
      onSplitAdd = {},
      onSplitDeleted = {},
      onSplitUpdated = { _, _ -> },
      onSplitRestored = {},
      onSplitDeleteFinalized = {},
      onRecClick = {},
      onOptionExpirationDateChanged = {},
      onOptionSectionChanged = {},
      onAddPriceAlert = {},
      onUpdatePriceAlert = {},
      onDeletePriceAlert = {},
      onAddNewHolding = {},
  )
}
