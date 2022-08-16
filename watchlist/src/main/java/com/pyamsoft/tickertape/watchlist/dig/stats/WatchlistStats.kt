package com.pyamsoft.tickertape.watchlist.dig.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.dig.DigKeyStatistics
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.watchlist.dig.MutableWatchlistDigViewState
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigViewState

@Composable
@JvmOverloads
internal fun WatchlistStats(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val stats = state.statistics

  DigKeyStatistics(
      modifier = modifier,
      isLoading = isLoading,
      statistics = stats,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewWatchlistStats() {
  val symbol = "MSFT".asSymbol()
  WatchlistStats(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              allowModifyWatchlist = true,
          ),
      onRefresh = {},
  )
}
