package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun TickerName(
    modifier: Modifier = Modifier,
    ticker: Ticker,
) {
  val symbol = ticker.symbol

  Column(
      modifier = modifier,
  ) {
    Text(
        text = symbol.symbol(),
        style = MaterialTheme.typography.body1,
    )
  }
}

@Preview
@Composable
private fun PreviewTickerName() {
  val symbol = "MSFT".asSymbol()
  Surface {
    TickerName(
        ticker =
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = newTestChart(symbol),
            ),
    )
  }
}
