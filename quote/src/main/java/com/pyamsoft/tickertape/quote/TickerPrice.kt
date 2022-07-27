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
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun TickerPrice(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    size: TickerSize,
) {
  val typography = MaterialTheme.typography
  val colors = MaterialTheme.colors

  val quote = ticker.quote

  val sizes =
      remember(size, typography) {
        when (size) {
          TickerSize.CHART -> TickerSizes.chart(typography)
          TickerSize.QUOTE -> TickerSizes.price(typography)
        }
      }

  if (quote != null) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
      val session = quote.currentSession

      val direction = session.direction
      val directionSign = session.direction.sign
      val composeColor =
          remember(direction, size, colors) {
            return@remember if (size == TickerSize.QUOTE || direction.isZero) {
              // If no direction or is a quote so bg is colored, unspecified
              Color.Unspecified
            } else {
              Color(direction.color)
            }
          }

      Text(
          text =
              when (session.state) {
                MarketState.REGULAR -> "Normal Market"
                MarketState.POST -> "After Hours"
                MarketState.PRE -> "Pre-Market"
              },
          style = MaterialTheme.typography.caption,
      )
      Text(
          text = session.price.display,
          style = sizes.title.copy(color = composeColor),
      )
      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            text = "${directionSign}${session.amount.display}",
            style = sizes.description.copy(color = composeColor),
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            text = "(${directionSign}${session.percent.display})",
            style = sizes.description.copy(color = composeColor),
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
        size = TickerSize.QUOTE,
    )
  }
}
