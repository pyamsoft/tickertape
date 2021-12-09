package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun WatchlistDigToolbar(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    onClose: () -> Unit,
) {
  TopAppBar(
      modifier = modifier,
      backgroundColor = MaterialTheme.colors.primary,
      contentColor = Color.White,
      title = {
        Text(
            text = symbol.symbol(),
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
  WatchlistDigToolbar(
      symbol = "MSFT".asSymbol(),
      onClose = {},
  )
}
