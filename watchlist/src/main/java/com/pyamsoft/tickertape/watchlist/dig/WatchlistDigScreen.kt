package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.dig.DigChart
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import com.pyamsoft.tickertape.watchlist.dig.news.WatchlistNews
import com.pyamsoft.tickertape.watchlist.dig.recs.WatchlistRecommendations

@Composable
@JvmOverloads
fun WatchlistDigScreen(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onTabUpdated: (WatchlistDigSections) -> Unit,
    onModifyWatchlist: () -> Unit,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      WatchlistDigToolbar(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          onClose = onClose,
          onModifyWatchlist = onModifyWatchlist,
          onTabUpdated = onTabUpdated,
      )

      Crossfade(
          modifier = Modifier.fillMaxWidth(),
          targetState = isLoading,
      ) { loading ->
        if (loading) {
          Loading(
              modifier = Modifier.fillMaxWidth(),
          )
        } else {
          Content(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              imageLoader = imageLoader,
              onScrub = onScrub,
              onRangeSelected = onRangeSelected,
              onRefresh = onRefresh,
          )
        }
      }
    }
  }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    imageLoader: ImageLoader,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onRefresh: () -> Unit,
) {
  val section = state.section

  Crossfade(
      modifier = modifier.fillMaxWidth(),
      targetState = section,
  ) { s ->
    return@Crossfade when (s) {
      WatchlistDigSections.CHART -> {
        DigChart(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onScrub = onScrub,
            onRangeSelected = onRangeSelected,
        )
      }
      WatchlistDigSections.NEWS -> {
        WatchlistNews(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onRefresh = onRefresh,
        )
      }
      WatchlistDigSections.STATISTICS -> {
        WatchlistStats(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
        )
      }
      WatchlistDigSections.RECOMMENDATIONS -> {
        WatchlistRecommendations(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
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
  ) { CircularProgressIndicator() }
}

@Preview
@Composable
private fun PreviewWatchlistDigScreen() {
  val symbol = "MSFT".asSymbol()
  WatchlistDigScreen(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              allowModifyWatchlist = true,
              equityType = EquityType.STOCK,
          ),
      imageLoader = createNewTestImageLoader(),
      onClose = {},
      onScrub = {},
      onRangeSelected = {},
      onModifyWatchlist = {},
      onTabUpdated = {},
      onRefresh = {},
  )
}
