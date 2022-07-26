package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme
import com.pyamsoft.tickertape.ui.ThemedCard

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

  ThemedCard(
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
      Row(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
      ) {
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
      quote.dayOpen?.also { open ->
        Info(
            modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
            name = "Open",
            value = open.display,
        )
      }

      quote.dayPreviousClose?.also { close ->
        Info(
            name = "Previous Close",
            value = close.display,
        )
      }
    }

    Row(
        modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      quote.dayLow?.also { low ->
        Info(
            modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
            name = "Low",
            value = low.display,
        )
      }
      quote.dayHigh?.also { high ->
        Info(
            modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
            name = "High",
            value = high.display,
        )
      }
      quote.dayVolume?.also { volume ->
        Info(
            modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
            name = "Volume",
            value = volume.display,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewQuote() {
  val symbol = "MSFT".asSymbol()
  PreviewTickerTapeTheme {
    Surface {
      Quote(
          modifier = Modifier.padding(16.dp),
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
  }
}
