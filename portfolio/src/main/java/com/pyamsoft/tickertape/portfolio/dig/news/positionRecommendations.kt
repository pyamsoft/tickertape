package com.pyamsoft.tickertape.portfolio.dig.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.quote.dig.DigNews
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
@JvmOverloads
internal fun PositionNews(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    imageLoader: ImageLoader,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val news = state.news

  DigNews(
      modifier = modifier,
      isLoading = isLoading,
      imageLoader = imageLoader,
      news = news,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewPositionNews() {
  val symbol = "MSFT".asSymbol()
  PositionNews(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
          ),
      onRefresh = {},
      imageLoader = createNewTestImageLoader(),
  )
}
