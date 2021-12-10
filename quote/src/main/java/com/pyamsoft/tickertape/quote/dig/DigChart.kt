package com.pyamsoft.tickertape.quote.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.QuoteDefaults
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.*
import java.time.LocalDateTime

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun DigChart(
    modifier: Modifier = Modifier,
    state: DigViewState,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  val ticker = state.ticker
  val range = state.range
  val currentDate = state.currentDate
  val currentPrice = state.currentPrice

  val chart = ticker.chart

  Column(
      modifier = modifier.padding(16.dp),
  ) {
    Crossfade(modifier = Modifier.fillMaxWidth(), targetState = chart) { c ->
      if (c == null) {
        Error(
            modifier = Modifier.fillMaxWidth(),
        )
      } else {
        Column(
            modifier = Modifier.fillMaxWidth().height(QuoteDefaults.CHART_HEIGHT_DP.dp),
        ) {
          Chart(
              modifier = Modifier.fillMaxWidth().weight(1F),
              chart = c,
              onScrub = onScrub,
          )
          CurrentScrub(
              modifier = Modifier.fillMaxWidth(),
              range = range,
              date = currentDate,
              price = currentPrice,
          )
        }
      }
    }

    Ranges(
        modifier = Modifier.fillMaxWidth(),
        range = range,
        onRangeSelected = onRangeSelected,
    )
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun CurrentScrub(
    modifier: Modifier = Modifier,
    range: StockChart.IntervalRange,
    date: LocalDateTime,
    price: StockMoneyValue?,
) {
  AnimatedVisibility(
      modifier = modifier,
      visible = price != null,
  ) {
    if (price != null) {
      val dateFormatter =
          if (range < StockChart.IntervalRange.THREE_MONTH) DATE_TIME_FORMATTER else DATE_FORMATTER
      Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
      ) {
        Text(
            text = date.format(dateFormatter.get().requireNotNull()),
            style = MaterialTheme.typography.body1,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = price.asMoneyValue(),
            style = MaterialTheme.typography.body1,
        )
      }
    }
  }
}

@Composable
private fun Ranges(
    modifier: Modifier = Modifier,
    range: StockChart.IntervalRange,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
) {
  LazyRow(
      modifier = modifier.padding(top = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(
        items = StockChart.IntervalRange.values(),
        key = { it.name },
    ) { item ->
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

@Composable
private fun Error(
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier,
  ) {
    Text(
        text = "Error loading chart",
        style =
            MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.error,
            ),
    )
  }
}

@Preview
@Composable
private fun PreviewDigChart() {
  val symbol = "MSFT".asSymbol()
  DigChart(
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
