package com.pyamsoft.tickertape.quote.dig.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.NewsDigViewState
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.BorderCard
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme
import com.pyamsoft.tickertape.ui.SwipeRefresh
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
@JvmOverloads
fun DigNews(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    state: NewsDigViewState,
    onRefresh: () -> Unit,
) {
  val error = state.newsError

  SwipeRefresh(
      modifier = modifier,
      isRefreshing = state.isLoading,
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.keylines.content),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {
      if (error == null) {
        items(
            items = state.news,
            key = { it.id },
        ) { n ->
          NewsItem(
              modifier = Modifier.fillMaxWidth(),
              news = n,
              imageLoader = imageLoader,
          )
        }
      } else {
        item {
          val errorMessage = remember(error) { error.message ?: "An unexpected error occurred" }

          Text(
              text = errorMessage,
              style =
                  MaterialTheme.typography.h6.copy(
                      color = MaterialTheme.colors.error,
                  ),
          )
        }
      }
    }
  }
}

@Composable
private fun NewsItem(
    modifier: Modifier = Modifier,
    news: StockNews,
    imageLoader: ImageLoader,
) {
  val title = news.title
  val description = news.description
  val source = news.sourceName
  val date = news.publishedAt
  val link = news.link
  val uriHandler = LocalUriHandler.current

  val displayDate = remember(date) { date?.format(DATE_FORMATTER.get().requireNotNull()) }

  BorderCard(
      modifier = modifier,
      borderColor = MaterialTheme.colors.secondary,
  ) {
    Column(
        modifier =
            Modifier.clickable { uriHandler.openUri(link) }
                .padding(MaterialTheme.keylines.baseline),
    ) {
      displayDate?.also { d ->
        Text(
            text = d,
            style =
                MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colors.onSurface,
                ),
        )
      }
      if (source.isNotBlank()) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.content),
            text = source,
            style =
                MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.W700,
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = ContentAlpha.medium,
                        ),
                ),
        )
      }
      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        if (news.imageUrl.isNotBlank()) {
          AsyncImage(
              modifier = Modifier.weight(0.25F).padding(end = MaterialTheme.keylines.baseline),
              model = news.imageUrl,
              imageLoader = imageLoader,
              contentScale = ContentScale.FillWidth,
              contentDescription = null,
          )
        }
        Column {
          if (title.isNotBlank()) {
            Text(
                modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
                text = title,
                style =
                    MaterialTheme.typography.body1.copy(
                        fontWeight = FontWeight.W700,
                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
          }
          if (description.isNotBlank()) {
            Text(
                text = description,
                style =
                    MaterialTheme.typography.body2.copy(
                        color =
                            MaterialTheme.colors.onSurface.copy(
                                alpha = ContentAlpha.medium,
                            ),
                    ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDigNews() {
  PreviewTickerTapeTheme {
    Surface {
      DigNews(
          modifier = Modifier.padding(16.dp),
          state =
              object :
                  MutableDigViewState(
                      symbol = "MSFT".asSymbol(),
                  ) {},
          onRefresh = {},
          imageLoader = createNewTestImageLoader(),
      )
    }
  }
}
