package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
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
    val typography = MaterialTheme.typography

    val labelStyle =
        remember(typography, nameColor) {
          typography.caption.copy(
              color = nameColor.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
              fontWeight = FontWeight.W400,
              fontSize = 10.sp,
          )
        }

    val contentStyle =
        remember(typography, valueColor) {
          typography.body2.copy(
              color = valueColor.copy(alpha = 1.0F),
              fontWeight = FontWeight.W400,
          )
        }

    val label = remember(name) { name.uppercase() }

    Column(
        modifier = modifier.padding(bottom = MaterialTheme.keylines.baseline),
        horizontalAlignment = Alignment.Start,
    ) {
      Text(
          text = label,
          style = labelStyle,
          maxLines = 1,
      )
      Text(
          text = value,
          style = contentStyle,
          maxLines = 1,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Quote(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    ticker: Ticker?,
    backgroundColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable QuoteScope.() -> Unit = {},
) {

  val isSpecialSession =
      remember(ticker) {
        if (ticker == null) {
          return@remember false
        } else {
          val quote = ticker.quote
          if (quote == null) {
            return@remember false
          } else {
            return@remember quote.afterHours != null || quote.preMarket != null
          }
        }
      }

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
      backgroundColor = backgroundColor,
      contentColor = QUOTE_CONTENT_DEFAULT_COLOR,
  ) {
    Column(
        modifier =
            Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(MaterialTheme.keylines.baseline)
                .fillMaxWidth(),
    ) {
      TickerName(
          modifier = Modifier.fillMaxWidth(),
          symbol = symbol,
          ticker = ticker,
          size = TickerSize.QUOTE,
      )

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
      ) {
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(
              modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
          ) {
            QuoteScopeInstance.content()
          }
        }

        Column(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            horizontalAlignment = Alignment.End,
        ) {
          if (isSpecialSession) {
            TickerPrice(
                modifier = Modifier.padding(bottom = MaterialTheme.keylines.content),
                ticker = ticker,
                size = TickerSize.QUOTE_EXTRA,
            )
          }

          TickerPrice(
              ticker = ticker,
              size = TickerSize.QUOTE,
          )
        }
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
          backgroundColor = Color.Unspecified,
          symbol = symbol,
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
