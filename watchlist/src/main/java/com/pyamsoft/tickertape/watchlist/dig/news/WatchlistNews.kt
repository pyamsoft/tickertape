package com.pyamsoft.tickertape.watchlist.dig.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.tickertape.quote.dig.DigNews
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import com.pyamsoft.tickertape.watchlist.dig.MutableWatchlistDigViewState
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigViewState

@Composable
@JvmOverloads
internal fun WatchlistNews(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    imageLoader: ImageLoader,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val news = state.news

  DigNews(
      modifier = modifier,
      isLoading = isLoading,
      news = news,
      imageLoader = imageLoader,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewWatchlistNews() {
  val symbol = "MSFT".asSymbol()
  WatchlistNews(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              allowModifyWatchlist = true,
          ),
      onRefresh = {},
      imageLoader = createNewTestImageLoader(),
  )
}
