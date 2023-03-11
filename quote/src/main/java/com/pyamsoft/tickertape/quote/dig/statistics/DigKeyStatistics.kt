package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.widget.SwipeRefresh
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.StatisticsDigViewState
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
fun DigKeyStatistics(
    modifier: Modifier = Modifier,
    state: StatisticsDigViewState,
    onRefresh: () -> Unit,
) {
  val error by state.statisticsError.collectAsState()
  val loadingState by state.loadingState.collectAsState()
  val statistics by state.statistics.collectAsState()

  val isRefreshing =
      remember(loadingState) { loadingState == BaseDigViewState.LoadingState.LOADING }

  SwipeRefresh(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
      if (error == null) {
        statistics?.let { stats ->
          renderFinancialHighlights(
              statistics = stats,
          )
          renderTradingInformation(
              statistics = stats,
          )
        }
      } else {
        item {
          val errorMessage =
              remember(error) { error.requireNotNull().message ?: "An unexpected error occurred" }

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
  val symbol = TestSymbol
  val clock = TestClock

  DigKeyStatistics(
      state =
          object :
              MutableDigViewState(
                  symbol = symbol,
                  clock = clock,
              ) {},
      onRefresh = {},
  )
}
