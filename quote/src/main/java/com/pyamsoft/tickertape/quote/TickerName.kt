package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun TickerName(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    size: TickerSize,
) {

  val symbol = ticker.symbol
  val quote = ticker.quote
  val typography = MaterialTheme.typography
    val contentColor = LocalContentColor.current

  val sizes =
      remember(size, typography, contentColor) {
        when (size) {
          TickerSize.CHART -> TickerSizes.chart(typography, contentColor)
          TickerSize.QUOTE -> TickerSizes.company(typography, contentColor)
        }
      }

  Column(
      modifier = modifier,
  ) {
    Text(
        text = symbol.raw,
        style = sizes.title,
    )

    if (quote != null) {
      if (quote.company.isValidCompany) {
        Text(
            text = quote.company.company,
            style = sizes.description,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
      }
    }
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
        size = TickerSize.QUOTE,
    )
  }
}
