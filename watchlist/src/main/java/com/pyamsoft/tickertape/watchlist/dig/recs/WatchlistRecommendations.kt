package com.pyamsoft.tickertape.watchlist.dig.recs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.dig.DigRecommendations
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.watchlist.dig.MutableWatchlistDigViewState
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigViewState

@Composable
@JvmOverloads
internal fun WatchlistRecommendations(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val recs = state.recommendations

  DigRecommendations(
      modifier = modifier,
      isLoading = isLoading,
      recommendations = recs,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewWatchlistRecommendations() {
  val symbol = "MSFT".asSymbol()
  WatchlistRecommendations(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              equityType = EquityType.STOCK,
              allowModifyWatchlist = false,
          ),
      onRefresh = {},
  )
}
