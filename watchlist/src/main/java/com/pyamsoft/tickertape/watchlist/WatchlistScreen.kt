package com.pyamsoft.tickertape.watchlist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.ui.FabDefaults
import com.pyamsoft.tickertape.ui.SearchBar
import com.pyamsoft.tickertape.watchlist.item.WatchlistItem

@Composable
@JvmOverloads
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    state: WatchlistViewState,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
    onSearchChanged: (String) -> Unit,
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
          navBarBottomHeight = navBarBottomHeight,
          onRefresh = onRefresh,
          onSelectTicker = onSelectTicker,
          onDeleteTicker = onDeleteTicker,
          onSearchChanged = onSearchChanged,
      )
    }
  }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: WatchlistViewState,
    navBarBottomHeight: Int,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
    onSearchChanged: (String) -> Unit,
    onRefresh: () -> Unit,
) {
  val error = state.error
  val tickers = state.watchlist
  val search = state.query

  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      Watchlist(
          modifier = Modifier.fillMaxSize(),
          tickers = tickers,
          navBarBottomHeight = navBarBottomHeight,
          onSelectTicker = onSelectTicker,
          onDeleteTicker = onDeleteTicker,
          search = search,
          onSearchChanged = onSearchChanged,
      )
    } else {
      Error(
          modifier = Modifier.fillMaxSize(),
          error = err,
          onRefresh = onRefresh,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Watchlist(
    modifier: Modifier = Modifier,
    tickers: List<Ticker>,
    search: String,
    navBarBottomHeight: Int,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
    onSearchChanged: (String) -> Unit,
) {
  val density = LocalDensity.current
  val bottomPaddingDp =
      remember(density, navBarBottomHeight) { density.run { navBarBottomHeight.toDp() } }

  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Spacer(
          modifier = Modifier.statusBarsHeight(),
      )
    }

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
            onSearchChanged = onSearchChanged,
        )
      }
    }

    items(
        items = tickers,
        key = { it.symbol.symbol() },
    ) { ticker ->
      WatchlistItem(
          modifier = Modifier.fillMaxWidth(),
          ticker = ticker,
          onSelect = onSelectTicker,
          onDelete = onDeleteTicker,
      )
    }

    item {
      Spacer(
          modifier =
              Modifier.navigationBarsHeight(
                  additional = bottomPaddingDp + FabDefaults.FAB_OFFSET_DP.dp,
              ),
      )
    }
  }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier,
    error: Throwable,
    onRefresh: () -> Unit,
) {
  Column(
      modifier = modifier.padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        textAlign = TextAlign.Center,
        text = error.message ?: "An unexpected error occurred",
        style =
            MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.error,
            ),
    )

    Text(
        modifier = Modifier.padding(top = 16.dp),
        textAlign = TextAlign.Center,
        text = "Please try again later.",
        style = MaterialTheme.typography.body2,
    )

    Button(
        modifier = Modifier.padding(top = 16.dp),
        onClick = onRefresh,
    ) {
      Text(
          text = "Refresh",
      )
    }
  }
}

@Preview
@Composable
private fun PreviewWatchlistScreen() {
  WatchlistScreen(
      state = MutableWatchlistViewState(),
      onRefresh = {},
      onDeleteTicker = {},
      onSelectTicker = {},
      onSearchChanged = {},
  )
}
