package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.StatisticsDigViewState
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
fun DigKeyStatistics(
    modifier: Modifier = Modifier,
    state: StatisticsDigViewState,
    onRefresh: () -> Unit,
) {
  val error = state.statisticsError

  SwipeRefresh(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
      if (error == null) {
        state.statistics?.let { stats ->
          renderFinancialHighlights(
              statistics = stats,
          )
          renderTradingInformation(
              statistics = stats,
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

@Preview
@Composable
private fun PreviewDigKeyStatistics() {
  DigKeyStatistics(
      state =
          object :
              MutableDigViewState(
                  symbol = "MSFT".asSymbol(),
              ) {},
      onRefresh = {},
  )
}
