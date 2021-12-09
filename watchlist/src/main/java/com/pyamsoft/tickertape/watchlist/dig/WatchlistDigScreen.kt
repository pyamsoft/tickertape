package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun WatchlistDigScreen(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onScrub: (Chart.Data?) -> Unit
) {
  val scaffoldState = rememberScaffoldState()

  val ticker = state.ticker
  val isLoading = state.isLoading

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
      topBar = {
        WatchlistDigToolbar(
            symbol = ticker.symbol,
            onClose = onClose,
        )
      },
  ) {
    SwipeRefresh(
        modifier = Modifier.fillMaxWidth(),
        state = rememberSwipeRefreshState(isRefreshing = isLoading),
        onRefresh = onRefresh,
    ) {
      Column(
          modifier =
              Modifier.fillMaxWidth().verticalScroll(state = rememberScrollState()).padding(16.dp),
      ) {
        DigChart(
            modifier = Modifier.fillMaxWidth().height(WatchlistDigDefaults.CHART_HEIGHT_DP.dp),
            ticker = ticker,
            onScrub = onScrub,
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun DigChart(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onScrub: (Chart.Data?) -> Unit
) {
  val chart = ticker.chart

  AnimatedVisibility(modifier = modifier, visible = chart != null) {
    Chart(
        chart = chart.requireNotNull(),
        onScrub = onScrub,
    )
  }
}

@Preview
@Composable
private fun PreviewWatchlistDigScreen() {
  WatchlistDigScreen(
      state =
          MutableWatchlistDigViewState(
              symbol = "MSFT".asSymbol(),
          ),
      onClose = {},
      onRefresh = {},
      onScrub = {},
  )
}
