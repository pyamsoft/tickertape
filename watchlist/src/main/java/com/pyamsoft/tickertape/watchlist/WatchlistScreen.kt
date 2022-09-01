package com.pyamsoft.tickertape.watchlist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.BaseListScreen
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.AnnaGoldScreen
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import com.pyamsoft.tickertape.watchlist.item.WatchlistItem
import kotlinx.coroutines.CoroutineScope

@Composable
@JvmOverloads
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
    onFabClick: () -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
) {
  BaseListScreen(
      modifier = modifier,
      navBarBottomHeight = navBarBottomHeight,
      imageLoader = imageLoader,
      isLoading = state.isLoading,
      pageError = state.error,
      list = state.watchlist,
      search = state.query,
      tab = state.section,
      onRefresh = onRefresh,
      onSearchChanged = onSearchChanged,
      onTabUpdated = onTabUpdated,
      onFabClick = onFabClick,
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
      onFabClick = {},
      onRegenerateList = {},
  )
}
