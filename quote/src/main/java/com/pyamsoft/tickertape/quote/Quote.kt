package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.BorderCard
import com.pyamsoft.tickertape.ui.PreviewTickerTapeTheme

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Quote(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    ticker: Ticker?,
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

  BorderCard(
      modifier = modifier,
  ) {
    Column(
        modifier =
            Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .fillMaxWidth()
                .padding(MaterialTheme.keylines.content),
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
            modifier = Modifier.weight(1F).padding(top = MaterialTheme.keylines.content),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          DefaultQuoteScopeInstance.content()
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
