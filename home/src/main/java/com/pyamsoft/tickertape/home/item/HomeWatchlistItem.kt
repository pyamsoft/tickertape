package com.pyamsoft.tickertape.home.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun HomeWatchlistItem(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onClick: (Ticker) -> Unit,
) {
  Quote(
      modifier = modifier,
      ticker = ticker,
      onClick = onClick,
      onLongClick = {},
  )
}

@Preview
@Composable
private fun PreviewHomeWatchlistItem() {
  val symbol = "MSFT".asSymbol()
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
