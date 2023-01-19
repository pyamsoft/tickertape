package com.pyamsoft.tickertape.portfolio.dig.chart

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.tickertape.quote.chart.ChartData
import com.pyamsoft.tickertape.quote.dig.ChartDigViewState
import com.pyamsoft.tickertape.quote.dig.chart.DigChart
import com.pyamsoft.tickertape.quote.test.newTestDigViewState
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
internal fun PortfolioChart(
    modifier: Modifier = Modifier,
    state: ChartDigViewState,
    imageLoader: ImageLoader,
    onScrub: (ChartData) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  DigChart(
      modifier = modifier,
      state = state,
      imageLoader = imageLoader,
      onScrub = onScrub,
      onRangeSelected = onRangeSelected,
  )
}

@Preview
@Composable
private fun PreviewPortfolioChart() {
  Surface {
    PortfolioChart(
        state = newTestDigViewState(),
        imageLoader = createNewTestImageLoader(),
        onScrub = {},
        onRangeSelected = {},
    )
  }
}
