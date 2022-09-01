package com.pyamsoft.tickertape.home.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.watchlist.item.WatchlistItem

@Composable
@JvmOverloads
fun HomeWatchlistItem(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onClick: (Ticker) -> Unit,
) {
  WatchlistItem(
      modifier = modifier,
      ticker = ticker,
      onSelect = onClick,
      onDelete = {},
  )
}

@Preview
@Composable
private fun PreviewHomeWatchlistItem() {
  val symbol = "MSFT".asSymbol()
  Surface {
    HomeWatchlistItem(
        ticker =
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = null,
            ),
        onClick = {},
    )
  }
}
