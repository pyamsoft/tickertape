package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
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

interface QuoteScope {

  @Composable
  fun Info(
      name: String,
      value: String,
  )

  @Composable
  fun Info(
      modifier: Modifier,
      name: String,
      value: String,
  )

  @Composable
  fun Info(
      modifier: Modifier,
      name: String,
      value: String,
      nameColor: Color,
      valueColor: Color,
  )
}

private object QuoteScopeInstance : QuoteScope {
  @Composable
  override fun Info(
      name: String,
      value: String,
  ) =
      Info(
          modifier = Modifier,
          name = name,
          value = value,
      )

  @Composable
  override fun Info(
      modifier: Modifier,
      name: String,
      value: String,
  ) =
      Info(
          modifier = modifier,
          name = name,
          value = value,
          nameColor = Color.Unspecified,
          valueColor = Color.Unspecified,
      )

  @Composable
  override fun Info(
      modifier: Modifier,
      name: String,
      value: String,
      nameColor: Color,
      valueColor: Color,
  ) {
    val captionStyle = MaterialTheme.typography.caption
    val textStyle =
        captionStyle.copy(
            color = captionStyle.color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
        )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text = name,
          style = textStyle,
          color = nameColor,
      )
      Text(
          modifier = Modifier.padding(start = MaterialTheme.keylines.typography),
          color = valueColor,
          text = value,
          style = textStyle.copy(fontWeight = FontWeight.W700),
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
      backgroundColor = rememberCardBackgroundColorForQuote(quote),
      contentColor = QUOTE_CONTENT_DEFAULT_COLOR,
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.baseline).fillMaxWidth(),
    ) {
      TickerName(
          ticker = ticker,
          size = TickerSize.QUOTE,
      )
      Row(
          modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
      ) {
        Column(
            modifier = Modifier.weight(1F),
        ) {
          if (quote != null) {
            QuoteScopeInstance.QuoteInfo(
                modifier = Modifier.fillMaxWidth(),
                quote = quote,
            )
          }
          QuoteScopeInstance.content()
        }

        TickerPrice(
            ticker = ticker,
            size = TickerSize.QUOTE,
        )
      }
    }
  }
}

@Composable
private fun QuoteScope.QuoteInfo(
    modifier: Modifier = Modifier,
    quote: StockQuote,
) {
  Column(
      modifier = modifier,
  ) {
    quote.dayPreviousClose?.also { close ->
      Info(
          name = "Previous Close",
          value = close.display,
      )
    }

    quote.dayOpen?.also { open ->
      Info(
          name = "Open",
          value = open.display,
      )
    }

    if (quote.dayLow != null || quote.dayHigh != null || quote.dayVolume != null) {
      Spacer(
          modifier = Modifier.height(MaterialTheme.keylines.baseline),
      )
    }

    quote.dayLow?.also { low ->
      Info(
          name = "Low",
          value = low.display,
      )
    }

    quote.dayHigh?.also { high ->
      Info(
          name = "High",
          value = high.display,
      )
    }

    quote.dayVolume?.also { volume ->
      Info(
          name = "Volume",
          value = volume.display,
      )
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
