package com.pyamsoft.tickertape.quote.dig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.stocks.api.StockNews

@Composable
@JvmOverloads
fun DigNews(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    news: List<StockNews>,
    onRefresh: () -> Unit,
) {

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
      modifier = modifier.clickable { uriHandler.openUri(link) },
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline),
    ) {
      if (source.isNotBlank()) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
            text = "From: $source",
            style = MaterialTheme.typography.caption,
        )
      }
      if (title.isNotBlank()) {
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      }
      if (description.isNotBlank()) {
        Text(
            text = description,
            style = MaterialTheme.typography.body2,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewWatchlistNews() {
  DigNews(
      isLoading = false,
      news = emptyList(),
      onRefresh = {},
  )
}
