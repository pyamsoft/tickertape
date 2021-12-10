package com.pyamsoft.tickertape.quote

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
fun DigChart(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    ticker: Ticker,
    range: StockChart.IntervalRange,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  Crossfade(modifier = modifier, targetState = isLoading) { loading ->
    if (loading) {
      Loading(
          modifier = Modifier.fillMaxWidth(),
      )
    } else {
      Content(
          modifier = Modifier.fillMaxWidth(),
          ticker = ticker,
          range = range,
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

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun Content(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    range: StockChart.IntervalRange,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val chart = ticker.chart

  Column(
      modifier = modifier.padding(16.dp),
  ) {
    AnimatedVisibility(visible = chart != null) {
      Chart(
          modifier = Modifier.height(QuoteDefaults.CHART_HEIGHT_DP.dp),
          chart = chart.requireNotNull(),
          onScrub = onScrub,
      )
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(items = StockChart.IntervalRange.values(), key = { it.name }) { item ->
        if (item == range) {
          Button(
              onClick = { onRangeSelected(item) },
          ) {
            Text(
                text = item.display,
            )
          }
        } else {
          TextButton(
              onClick = { onRangeSelected(item) },
          ) {
            Text(
                text = item.display,
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDigChart() {
  val symbol = "MSFT".asSymbol()
  DigChart(
      isLoading = false,
      ticker =
          Ticker(
              symbol = symbol,
              quote = newTestQuote(symbol),
              chart = newTestChart(symbol),
          ),
      range = StockChart.IntervalRange.ONE_DAY,
      onScrub = {},
      onRangeSelected = {},
  )
}
