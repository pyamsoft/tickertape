package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
    ticker: Ticker?,
    sort: QuoteSort?,
    size: TickerSize,
) {
  val quote = ticker?.quote
  if (quote != null) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colors
    val contentColor = LocalContentColor.current

    val sizes =
        remember(size, typography, contentColor) {
          when (size) {
            TickerSize.CHART -> TickerSizes.chart(typography, contentColor)
            TickerSize.QUOTE -> TickerSizes.price(typography, contentColor)
          }
        }

    val session =
        when (sort) {
          QuoteSort.PRE_MARKET -> quote.preMarket ?: quote.regular
          QuoteSort.REGULAR -> quote.regular
          QuoteSort.AFTER_HOURS -> quote.afterHours ?: quote.regular
          null -> quote.currentSession
        }

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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
      Text(
          text =
              when (session.state) {
                MarketState.REGULAR -> "Normal Market"
                MarketState.POST -> "After Hours"
                MarketState.PRE -> "Pre-Market"
              },
          style = MaterialTheme.typography.caption,
      )
      PriceSection(
          value = session.price.display,
          valueStyle = sizes.title.copy(color = composeColor),
          changeAmount = "${directionSign}${session.amount.display}",
          changePercent = "(${directionSign}${session.percent.display})",
          changeStyle = sizes.description.copy(color = composeColor),
      )
    }
  }
}

@Composable
@JvmOverloads
fun PriceSection(
    modifier: Modifier = Modifier,
    value: String,
    valueStyle: TextStyle,
    changeAmount: String,
    changePercent: String,
    changeStyle: TextStyle,
) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.End,
  ) {
    if (value.isNotBlank()) {
      Text(
          text = value,
          style = valueStyle,
      )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      if (changeAmount.isNotBlank()) {
        Text(
            text = changeAmount,
            style = changeStyle,
        )
      }
      if (changePercent.isNotBlank()) {
        Text(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            text = changePercent,
            style = changeStyle,
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
        sort = null,
        size = TickerSize.QUOTE,
    )
  }
}
