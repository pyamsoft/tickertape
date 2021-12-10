package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.dig.DigChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun WatchlistDigScreen(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onClose: () -> Unit,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val scaffoldState = rememberScaffoldState()

  val isLoading = state.isLoading
  val ticker = state.ticker

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
      topBar = {
        WatchlistDigToolbar(
            ticker = ticker,
            onClose = onClose,
        )
      },
  ) {
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
            onScrub = onScrub,
            onRangeSelected = onRangeSelected,
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
      modifier = modifier.padding(16.dp),
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
          ),
      onClose = {},
      onScrub = {},
      onRangeSelected = {},
  )
}
