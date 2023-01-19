package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.chart.ChartData
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.chart.DigChart
import com.pyamsoft.tickertape.quote.dig.news.DigNews
import com.pyamsoft.tickertape.quote.dig.options.DigOptionsChain
import com.pyamsoft.tickertape.quote.dig.pricealert.DigPriceAlerts
import com.pyamsoft.tickertape.quote.dig.recommend.DigRecommendations
import com.pyamsoft.tickertape.quote.dig.statistics.DigKeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest

@Composable
@OptIn(ExperimentalPagerApi::class)
fun WatchlistDigScreen(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onTabUpdated: (WatchlistDigSections) -> Unit,
    onRefresh: () -> Unit,
    onModifyWatchlist: () -> Unit,
    // Chart
    onChartScrub: (ChartData) -> Unit,
    onChartRangeSelected: (StockChart.IntervalRange) -> Unit,
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

  val allTabs = rememberTabs(ticker)
  val pagerState = rememberPagerState()

  val handleTabUpdated by rememberUpdatedState(onTabUpdated)

  // Watch for a swipe causing a page change and update accordingly
  LaunchedEffect(
      pagerState,
      allTabs,
  ) {
    snapshotFlow { pagerState.currentPage }
        .collectLatest { index ->
          val page = allTabs[index]
          handleTabUpdated(page)
        }
  }

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
    ) {
      WatchlistDigToolbar(
          modifier = Modifier.fillMaxWidth().zIndex(1F),
          state = state,
          pagerState = pagerState,
          allTabs = allTabs,
          onClose = onClose,
          onModifyWatchlist = onModifyWatchlist,
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
                state = state,
                imageLoader = imageLoader,
                pagerState = pagerState,
                allTabs = allTabs,
                onChartScrub = onChartScrub,
                onChartRangeSelected = onChartRangeSelected,
                onRefresh = onRefresh,
                onRecClick = onRecClick,
                onOptionSectionChanged = onOptionSectionChanged,
                onOptionExpirationDateChanged = onOptionExpirationDateChanged,
                onAddPriceAlert = onAddPriceAlert,
                onUpdatePriceAlert = onUpdatePriceAlert,
                onDeletePriceAlert = onDeletePriceAlert,
            )
          }
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
private fun Content(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    imageLoader: ImageLoader,
    pagerState: PagerState,
    allTabs: SnapshotStateList<WatchlistDigSections>,
    onRefresh: () -> Unit,
    // Chart
    onChartScrub: (ChartData) -> Unit,
    onChartRangeSelected: (StockChart.IntervalRange) -> Unit,
    // Recommendations
    onRecClick: (Ticker) -> Unit,
    // Options
    onOptionSectionChanged: (StockOptions.Contract.Type) -> Unit,
    onOptionExpirationDateChanged: (LocalDate) -> Unit,
    // Price alerts
    onAddPriceAlert: () -> Unit,
    onUpdatePriceAlert: (PriceAlert) -> Unit,
    onDeletePriceAlert: (PriceAlert) -> Unit,
) {
  HorizontalPager(
      modifier = modifier,
      count = allTabs.size,
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
      WatchlistDigSections.CHART -> {
        DigChart(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onScrub = onChartScrub,
            onRangeSelected = onChartRangeSelected,
        )
      }
      WatchlistDigSections.PRICE_ALERTS -> {
        DigPriceAlerts(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onAddPriceAlert = onAddPriceAlert,
            onUpdatePriceAlert = onUpdatePriceAlert,
            onDeletePriceAlert = onDeletePriceAlert,
        )
      }
      WatchlistDigSections.NEWS -> {
        DigNews(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onRefresh = onRefresh,
        )
      }
      WatchlistDigSections.STATISTICS -> {
        DigKeyStatistics(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
        )
      }
      WatchlistDigSections.RECOMMENDATIONS -> {
        DigRecommendations(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onRecClick = onRecClick,
        )
      }
      WatchlistDigSections.OPTIONS_CHAIN -> {
        DigOptionsChain(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onExpirationDateChanged = onOptionExpirationDateChanged,
            onSectionChanged = onOptionSectionChanged,
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
private fun PreviewWatchlistDigScreen() {
  val symbol = "MSFT".asSymbol()
  WatchlistDigScreen(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
          ),
      imageLoader = createNewTestImageLoader(),
      onClose = {},
      onChartScrub = {},
      onChartRangeSelected = {},
      onModifyWatchlist = {},
      onTabUpdated = {},
      onRefresh = {},
      onRecClick = {},
      onOptionExpirationDateChanged = {},
      onOptionSectionChanged = {},
      onAddPriceAlert = {},
      onUpdatePriceAlert = {},
      onDeletePriceAlert = {},
  )
}
