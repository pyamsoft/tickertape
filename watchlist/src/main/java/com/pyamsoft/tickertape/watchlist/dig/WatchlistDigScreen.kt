package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
@JvmOverloads
fun WatchlistDigScreen(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onModifyWatchlist: () -> Unit,
) {
  val isLoading = state.isLoading

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.DialogElevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      WatchlistDigToolbar(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          onClose = onClose,
          onModifyWatchlist = onModifyWatchlist,
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
          DigChart(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              imageLoader = imageLoader,
              onScrub = onScrub,
              onRangeSelected = onRangeSelected,
          )
        }
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
  WatchlistDigScreen(
      state =
          MutableWatchlistDigViewState(
              symbol = "MSFT".asSymbol(),
              allowModifyWatchlist = true,
          ),
      imageLoader = createNewTestImageLoader(),
      onClose = {},
      onScrub = {},
      onRangeSelected = {},
      onModifyWatchlist = {},
  )
}
