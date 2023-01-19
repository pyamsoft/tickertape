package com.pyamsoft.tickertape.watchlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.base.BaseListScreen
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.AnnaGoldScreen
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import com.pyamsoft.tickertape.watchlist.item.WatchlistItem
import kotlinx.coroutines.CoroutineScope

@Composable
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    state: WatchlistViewState,
    imageLoader: ImageLoader,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
) {
  val loadingState by state.loadingState.collectAsState()
  val pageError by state.error.collectAsState()
  val list by state.watchlist.collectAsState()
  val search by state.query.collectAsState()
  val tab by state.section.collectAsState()

  val isLoading = remember(loadingState) { loadingState == WatchlistViewState.LoadingState.LOADING }

  BaseListScreen(
      modifier = modifier,
      navBarBottomHeight = navBarBottomHeight,
      imageLoader = imageLoader,
      isLoading = isLoading,
      pageError = pageError,
      list = list,
      search = search,
      tab = tab,
      onRefresh = onRefresh,
      onSearchChanged = onSearchChanged,
      onTabUpdated = onTabUpdated,
      onRegenerateList = onRegenerateList,
      itemKey = { index, stock -> "${stock.symbol.raw}-${index}" },
      renderListItem = { stock ->
        WatchlistItem(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.baseline),
            ticker = stock,
            onSelect = onSelectTicker,
            onDelete = onDeleteTicker,
        )
      },
      renderEmptyState = {
        EmptyState(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.baseline),
            imageLoader = imageLoader,
        )
      },
  )
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
) {
  AnnaGoldScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = R.drawable.watchlist_empty,
      bottomContent = {
        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.keylines.content),
            text = "Not watching anything, add something!",
            style = MaterialTheme.typography.h6,
        )
      },
  )
}

@Preview
@Composable
private fun PreviewWatchlistScreen() {
  WatchlistScreen(
      state = MutableWatchlistViewState(),
      imageLoader = createNewTestImageLoader(),
      onRefresh = {},
      onDeleteTicker = {},
      onSelectTicker = {},
      onSearchChanged = {},
      onTabUpdated = {},
      onRegenerateList = {},
  )
}
