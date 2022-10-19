package com.pyamsoft.tickertape.watchlist.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun WatchlistItem(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onSelect: (Ticker) -> Unit,
    onDelete: (Ticker) -> Unit,
) {
  val quote = ticker.quote

  Box(
      modifier = modifier,
  ) {
    Quote(
        modifier = Modifier.fillMaxWidth(),
        symbol = ticker.symbol,
        ticker = ticker,
        onClick = { onSelect(ticker) },
        onLongClick = { onDelete(ticker) },
    ) {
      if (quote != null) {
        Column {
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
    }
  }
}

@Preview
@Composable
private fun PreviewWatchlistItem() {
  val symbol = "MSFT".asSymbol()
  Surface {
    WatchlistItem(
        ticker =
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = null,
            ),
        onSelect = {},
        onDelete = {},
    )
  }
}
