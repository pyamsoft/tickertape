package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun Quote(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onClick: (Ticker) -> Unit,
    content: @Composable () -> Unit = {},
) {
  val quote = ticker.quote

  Card(
      modifier = modifier.clickable { onClick(ticker) },
      elevation = 2.dp,
  ) {
    Column(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
    ) {
      Row(modifier = Modifier.padding(bottom = 8.dp)) {
        TickerName(
            modifier = Modifier.weight(1F),
            ticker = ticker,
        )
        TickerPrice(
            ticker = ticker,
        )
      }
      if (quote != null) {
        QuoteInfo(
            modifier = Modifier.fillMaxWidth(),
            quote = quote,
        )
      }
      content()
    }
  }
}

@Composable
private fun QuoteInfo(modifier: Modifier = Modifier, quote: StockQuote) {
  Column(
      modifier = modifier,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Info(
          modifier = Modifier.padding(end = 8.dp),
          name = "Open",
          value = quote.dayOpen().asMoneyValue(),
      )

      val previousClose = quote.dayPreviousClose()
      if (previousClose != null) {
        Info(
            name = "Previous Close",
            value = previousClose.asMoneyValue(),
        )
      }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Info(
          modifier = Modifier.padding(end = 8.dp),
          name = "Low",
          value = quote.dayLow().asMoneyValue(),
      )
      Info(
          modifier = Modifier.padding(end = 8.dp),
          name = "High",
          value = quote.dayHigh().asMoneyValue(),
      )
      Info(
          modifier = Modifier.padding(end = 8.dp),
          name = "Volume",
          value = quote.dayVolume().asVolumeValue(),
      )
    }
  }
}

@Composable
private fun Info(modifier: Modifier = Modifier, name: String, value: String) {
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        text = name,
        style = MaterialTheme.typography.caption,
    )
    Text(
        modifier = Modifier.padding(start = 4.dp),
        text = value,
        style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
  }
}

@Preview
@Composable
private fun PreviewQuote() {
  val symbol = "MSFT".asSymbol()
  Quote(
      ticker =
          Ticker(
              symbol = symbol,
              quote = newTestQuote(symbol),
              chart = null,
          ),
      onClick = {},
  )
}
