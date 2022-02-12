package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

interface QuoteScope {

  @Composable
  fun Info(
      name: String,
      value: String,
  ) {
    Info(
        modifier = Modifier,
        name = name,
        value = value,
        valueColor = Color.Unspecified,
    )
  }

  @Composable
  fun Info(
      modifier: Modifier,
      name: String,
      value: String,
  ) {
    Info(
        modifier = modifier,
        name = name,
        value = value,
        valueColor = Color.Unspecified,
    )
  }

  @Composable
  fun Info(
      name: String,
      value: String,
      valueColor: Color,
  ) {
    Info(
        modifier = Modifier,
        name = name,
        value = value,
        valueColor = valueColor,
    )
  }

  @Composable fun Info(modifier: Modifier, name: String, value: String, valueColor: Color)
}

private object QuoteScopeInstance : QuoteScope {

  @Composable
  override fun Info(
      modifier: Modifier,
      name: String,
      value: String,
      valueColor: Color,
  ) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = name,
          style = MaterialTheme.typography.caption,
      )
      Text(
          modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
          color = valueColor,
          text = value,
          style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
@JvmOverloads
@OptIn(ExperimentalFoundationApi::class)
fun Quote(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onClick: (Ticker) -> Unit,
    onLongClick: (Ticker) -> Unit,
    content: @Composable QuoteScope.() -> Unit = {},
) {
  val quote = ticker.quote

  Card(
      modifier =
          modifier.combinedClickable(
              onClick = { onClick(ticker) },
              onLongClick = { onLongClick(ticker) },
          ),
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline).fillMaxWidth(),
    ) {
      Row(modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline)) {
        TickerName(
            modifier = Modifier.weight(1F),
            ticker = ticker,
        )
        TickerPrice(
            ticker = ticker,
        )
      }
      if (quote != null) {
        QuoteScopeInstance.QuoteInfo(
            modifier = Modifier.fillMaxWidth(),
            quote = quote,
        )
      }
      QuoteScopeInstance.content()
    }
  }
}

@Composable
private fun QuoteScope.QuoteInfo(modifier: Modifier = Modifier, quote: StockQuote) {
  Column(
      modifier = modifier,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
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
        modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          name = "Low",
          value = quote.dayLow().asMoneyValue(),
      )
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          name = "High",
          value = quote.dayHigh().asMoneyValue(),
      )
      Info(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          name = "Volume",
          value = quote.dayVolume().asVolumeValue(),
      )
    }
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
      onLongClick = {},
  )
}
