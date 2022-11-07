package com.pyamsoft.tickertape.quote.dig.alert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
fun DigAlerts(
    modifier: Modifier = Modifier,
    state: DigViewState,
    onRefresh: () -> Unit,
) {
  SwipeRefresh(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {}
  }
}

@Preview
@Composable
private fun PreviewDigAlerts() {
  DigAlerts(
      state =
          object :
              MutableDigViewState(
                  symbol = "MSFT".asSymbol(),
              ) {},
      onRefresh = {},
  )
}
