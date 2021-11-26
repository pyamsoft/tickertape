package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.currentSession

@Composable
@JvmOverloads
fun TickerPrice(
    modifier: Modifier = Modifier,
    ticker: Ticker,
) {
  val quote = ticker.quote

  if (quote != null) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
      val session = quote.currentSession()

      val direction = session.direction()
      val directionSign = session.direction().sign()
      val composeColor =
          if (direction.isZero()) {
            MaterialTheme.typography.caption.color
          } else {
            remember(direction) {
              val color = session.direction().color()
              Color(color)
            }
          }

      Text(
          text =
              when (session.state()) {
                MarketState.REGULAR -> "Normal Market"
                MarketState.POST -> "After Hours"
                MarketState.PRE -> "Pre-Market"
              },
          style = MaterialTheme.typography.caption,
      )
      Text(
          text = session.price().asMoneyValue(),
          style = MaterialTheme.typography.body2.copy(color = composeColor),
      )
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${directionSign}${session.amount().asMoneyValue()}",
            style = MaterialTheme.typography.caption.copy(color = composeColor),
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "(${directionSign}${session.percent().asPercentValue()})",
            style = MaterialTheme.typography.caption.copy(color = composeColor),
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewTickerPrice() {
  val symbol = "MSFT".asSymbol()
  Surface {
    TickerPrice(
        ticker =
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = newTestChart(symbol),
            ),
    )
  }
}
