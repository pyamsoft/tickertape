package com.pyamsoft.tickertape.watchlist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker

@Composable
@JvmOverloads
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    state: WatchlistViewState,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
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
    onRefresh: () -> Unit,
) {
  val error = state.error
  val tickers = state.watchlist

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
private fun Watchlist(
    modifier: Modifier = Modifier,
    tickers: List<Ticker>,
    navBarBottomHeight: Int,
    onSelectTicker: (Ticker) -> Unit,
    onDeleteTicker: (Ticker) -> Unit,
) {
  val density = LocalDensity.current
  val bottomPaddingDp =
      remember(density, navBarBottomHeight) { density.run { navBarBottomHeight.toDp() } }

  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(items = tickers, key = { it.symbol.symbol() }) { ticker ->
      Quote(
          ticker = ticker,
          onClick = onSelectTicker,
          onLongClick = onDeleteTicker,
      )
    }

    item {
      Spacer(
          modifier = Modifier.navigationBarsHeight(additional = bottomPaddingDp),
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
  )
}
