package com.pyamsoft.tickertape.portfolio.dig.chart

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.dig.DigChart
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.test.newTestDigViewState
import com.pyamsoft.tickertape.stocks.api.StockChart

@Composable
internal fun PorfolioChart(
    modifier: Modifier = Modifier,
    state: DigViewState,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  DigChart(
      modifier = modifier,
      state = state,
      onScrub = onScrub,
      onRangeSelected = onRangeSelected,
  )
}

@Composable
@Preview
private fun PreviewPortfolioChart() {
  Surface {
    PorfolioChart(
        state = newTestDigViewState(),
        onScrub = {},
        onRangeSelected = {},
    )
  }
}
