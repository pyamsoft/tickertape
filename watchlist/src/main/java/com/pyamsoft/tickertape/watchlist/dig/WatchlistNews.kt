package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.dig.DigNews
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun WatchlistNews(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val news = state.news

  DigNews(
      modifier = modifier,
      isLoading = isLoading,
      news = news,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewWatchlistNews() {
  WatchlistNews(
      state =
          MutableWatchlistDigViewState(
              symbol = "MSFT".asSymbol(),
              allowModifyWatchlist = true,
              equityType = EquityType.STOCK,
          ),
      onRefresh = {},
  )
}
