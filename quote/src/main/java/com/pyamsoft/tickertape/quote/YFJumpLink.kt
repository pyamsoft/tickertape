package com.pyamsoft.tickertape.quote

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.icon.OpenInNew

@Composable
fun YFJumpLink(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
) {
  val uriHandler = LocalUriHandler.current

  IconButton(
      modifier = modifier,
      onClick = { uriHandler.openUri("https://finance.yahoo.com/quote/${symbol.raw}") },
  ) {
    Icon(
        imageVector = Icons.Filled.OpenInNew,
        contentDescription = "View ${symbol.raw} on Yahoo Finance",
    )
  }
}
