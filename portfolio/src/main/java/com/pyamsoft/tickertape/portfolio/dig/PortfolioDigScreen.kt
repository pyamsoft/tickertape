package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.dig.DigChart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun PortfolioDigScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onClose: () -> Unit,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val ticker = state.ticker

  Surface(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      PortfolioDigToolbar(
          ticker = ticker,
          onClose = onClose,
      )

      ChartPage(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          onScrub = onScrub,
          onRangeSelected = onRangeSelected,
      )
    }
  }
}

@Composable
private fun ChartPage(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val isLoading = state.isLoading

  Crossfade(
      modifier = modifier,
      targetState = isLoading,
  ) { loading ->
    if (loading) {
      Loading(
          modifier = Modifier.fillMaxWidth(),
      )
    } else {
      DigChart(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          onScrub = onScrub,
          onRangeSelected = onRangeSelected,
      )
    }
  }
}

@Composable
private fun Loading(
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier.padding(16.dp),
      contentAlignment = Alignment.Center,
  ) { CircularProgressIndicator() }
}

@Preview
@Composable
private fun PreviewPortfolioDigScreen() {
  PortfolioDigScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = "MSFT".asSymbol(),
          ),
      onClose = {},
      onScrub = {},
      onRangeSelected = {},
  )
}
