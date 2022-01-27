package com.pyamsoft.tickertape.portfolio.dig.chart

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.dig.DigChart
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.test.newTestDigViewState
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
internal fun PortfolioChart(
    modifier: Modifier = Modifier,
    state: DigViewState,
    imageLoader: ImageLoader,
    onScrub: (Chart.Data?) -> Unit,
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

@Composable
@Preview
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
