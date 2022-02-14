package com.pyamsoft.tickertape.watchlist.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun WatchlistItem(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onSelect: (Ticker) -> Unit,
    onDelete: (Ticker) -> Unit,
) {
  Box(
      modifier = modifier,
  ) {
    Quote(
        modifier = Modifier.fillMaxWidth(),
        ticker = ticker,
        onClick = onSelect,
        onLongClick = onDelete,
    )
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
