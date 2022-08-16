package com.pyamsoft.tickertape.portfolio.dig.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.quote.dig.DigKeyStatistics
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PositionStats(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val statistics = state.statistics

  DigKeyStatistics(
      modifier = modifier,
      isLoading = isLoading,
      statistics = statistics,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewPositionStats() {
  val symbol = "MSFT".asSymbol()
  PositionStats(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
          ),
      onRefresh = {},
  )
}
