package com.pyamsoft.tickertape.watchlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.ImageLoader
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.SearchBar
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.add.NewTickerFab
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.AnnaGoldScreen
import com.pyamsoft.tickertape.ui.ErrorScreen
import com.pyamsoft.tickertape.ui.FabDefaults
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import com.pyamsoft.tickertape.watchlist.item.WatchlistItem

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
) {
  val loading = state.isLoading

  val scaffoldState = rememberScaffoldState()
  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
  ) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = loading),
        onRefresh = onRefresh,
    ) {
      Content(
          modifier = Modifier.fillMaxSize(),
          state = state,
          imageLoader = imageLoader,
          navBarBottomHeight = navBarBottomHeight,
          onRefresh = onRefresh,
          onSelectTicker = onSelectTicker,
          onDeleteTicker = onDeleteTicker,
          onSearchChanged = onSearchChanged,
          onTabUpdated = onTabUpdated,
          onFabClick = onFabClick,
      )
    }
  }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: WatchlistViewState,
    imageLoader: ImageLoader,
    navBarBottomHeight: Int,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
    onSearchChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onFabClick: () -> Unit,
) {
  val isLoading = state.isLoading

  val density = LocalDensity.current
  val bottomPaddingDp =
      remember(
          density,
          navBarBottomHeight,
      ) { density.run { navBarBottomHeight.toDp() } }
  val contentPadding = MaterialTheme.keylines.content
  val fabBottomPadding =
      remember(bottomPaddingDp, contentPadding) { bottomPaddingDp + contentPadding }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomCenter,
  ) {
    Watchlist(
        modifier = Modifier.fillMaxSize(),
        state = state,
        imageLoader = imageLoader,
        navBarBottomHeight = bottomPaddingDp,
        onSelectTicker = onSelectTicker,
        onDeleteTicker = onDeleteTicker,
        onSearchChanged = onSearchChanged,
        onTabUpdated = onTabUpdated,
        onRefresh = onRefresh,
    )

    NewTickerFab(
        visible = !isLoading,
        modifier =
            Modifier.padding(MaterialTheme.keylines.content)
                .navigationBarsPadding(bottom = true)
                .padding(bottom = fabBottomPadding),
        onClick = onFabClick,
    )
  }
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

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Watchlist(
    modifier: Modifier = Modifier,
    state: WatchlistViewState,
    imageLoader: ImageLoader,
    navBarBottomHeight: Dp,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRefresh: () -> Unit,
) {
  val tickers = state.watchlist
  val search = state.query
  val tab = state.section
  val error = state.error
  val isEmptyList = remember(tickers) { tickers.isEmpty() }

  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = MaterialTheme.keylines.baseline),
  ) {
    stickyHeader {
      Column(
          modifier = Modifier.fillMaxWidth(),
      ) {
        Spacer(
            modifier = Modifier.statusBarsHeight(),
        )
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            search = search,
            currentTab = tab,
            onSearchChanged = onSearchChanged,
            onTabUpdated = onTabUpdated,
        )
      }
    }

    when {
      error != null -> {
        item {
          ErrorState(
              modifier = Modifier.fillMaxSize(),
              imageLoader = imageLoader,
              error = error,
              onRefresh = onRefresh,
          )
        }
      }
      isEmptyList -> {
        item {
          EmptyState(
              modifier = Modifier.fillMaxWidth(),
              imageLoader = imageLoader,
          )
        }
      }
      else -> {
        itemsIndexed(
            items = tickers,
            key = { _, ticker -> ticker.symbol.symbol() },
        ) { index, ticker ->
          if (index == 0) {
            Spacer(
                modifier = Modifier.height(MaterialTheme.keylines.content),
            )
          }

          WatchlistItem(
              modifier = Modifier.fillMaxWidth(),
              ticker = ticker,
              onSelect = onSelectTicker,
              onDelete = onDeleteTicker,
          )

          Spacer(
              modifier = Modifier.height(MaterialTheme.keylines.content),
          )
        }
      }
    }

    item {
      Spacer(
          modifier =
              Modifier.navigationBarsHeight(
                  additional =
                      navBarBottomHeight +
                          FabDefaults.FAB_OFFSET_DP +
                          (MaterialTheme.keylines.content * 2),
              ),
      )
    }
  }
}

@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    error: Throwable,
    onRefresh: () -> Unit,
) {
  ErrorScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      bottomContent = {
        Text(
            textAlign = TextAlign.Center,
            text = error.message ?: "An unexpected error occurred",
            style =
                MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.error,
                ),
        )

        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            textAlign = TextAlign.Center,
            text = "Please try again later.",
            style = MaterialTheme.typography.body2,
        )

        Button(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            onClick = onRefresh,
        ) {
          Text(
              text = "Refresh",
          )
        }
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
  )
}
