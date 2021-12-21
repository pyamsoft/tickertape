package com.pyamsoft.tickertape.portfolio.dig.chart

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigChart
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDateTime

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
  val symbol = "MSFT".asSymbol()
  Surface {
    PorfolioChart(
        state =
            object : DigViewState {
              override val ticker =
                  Ticker(
                      symbol = symbol,
                      quote = newTestQuote(symbol),
                      chart = newTestChart(symbol),
                  )

              override val range = StockChart.IntervalRange.ONE_DAY

              override val currentDate = LocalDateTime.now()

              override val currentPrice = 1.0.asMoney()
            },
        onScrub = {},
        onRangeSelected = {},
    )
  }
}
