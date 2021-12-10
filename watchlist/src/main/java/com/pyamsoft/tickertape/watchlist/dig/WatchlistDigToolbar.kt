package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun WatchlistDigToolbar(
    modifier: Modifier = Modifier,
    ticker: Ticker,
    onClose: () -> Unit,
) {
  val title = ticker.quote?.company()?.company() ?: ticker.symbol.symbol()

  TopAppBar(
      modifier = modifier,
      backgroundColor = MaterialTheme.colors.primary,
      contentColor = Color.White,
      title = {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
      },
      navigationIcon = {
        IconButton(
            onClick = onClose,
        ) {
          Icon(
              imageVector = Icons.Filled.Close,
              contentDescription = "Close",
          )
        }
      },
  )
}

@Preview
@Composable
private fun PreviewWatchlistDigToolbar() {
  val symbol = "MSFT".asSymbol()
  WatchlistDigToolbar(
      ticker =
          Ticker(
              symbol = symbol,
              quote = newTestQuote(symbol),
              chart = null,
          ),
      onClose = {},
  )
}
