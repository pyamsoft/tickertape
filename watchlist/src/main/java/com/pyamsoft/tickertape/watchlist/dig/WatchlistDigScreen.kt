package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.DigChart
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
  val range = state.range

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
    DigChart(
        modifier = Modifier.fillMaxWidth(),
        isLoading = isLoading,
        ticker = ticker,
        range = range,
        onScrub = onScrub,
        onRangeSelected = onRangeSelected,
    )
  }
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
