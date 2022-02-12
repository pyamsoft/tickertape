package com.pyamsoft.tickertape.portfolio.dig.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PositionNews(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val news = state.news

  SwipeRefresh(
      modifier = modifier,
      state = rememberSwipeRefreshState(isRefreshing = isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        contentPadding = PaddingValues(MaterialTheme.keylines.content),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {
      items(
          items = news,
          key = { it.id() },
      ) { n ->
        NewsItem(
            modifier = Modifier.fillMaxWidth(),
            news = n,
        )
      }
    }
  }
}

@Composable
private fun NewsItem(
    modifier: Modifier = Modifier,
    news: StockNews,
) {
  val title = news.title()
  val description = news.description()
  val source = news.sourceName()
  val link = news.link()
  val uriHandler = LocalUriHandler.current

  Card(
      modifier =
          modifier.clickable { uriHandler.openUri(link) }.padding(MaterialTheme.keylines.baseline),
      elevation = CardDefaults.Elevation,
  ) {
    if (source.isNotBlank()) {
      Text(
          text = "From: $source",
          style = MaterialTheme.typography.caption,
      )
    }
    if (title.isNotBlank()) {
      Text(
          text = title,
          style = MaterialTheme.typography.body1,
      )
    }
    if (description.isNotBlank()) {
      Text(
          text = description,
          style = MaterialTheme.typography.body2,
      )
    }
  }
}

@Preview
@Composable
private fun PreviewPositionNews() {
  PositionNews(
      state =
          MutablePortfolioDigViewState(
              symbol = "MSFT".asSymbol(),
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
          ),
      onRefresh = {},
  )
}
