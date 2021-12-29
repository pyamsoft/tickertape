package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun WatchlistDigToolbar(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onClose: () -> Unit,
    onModifyWatchlist: () -> Unit,
) {
  val isLoading = state.isLoading
  val ticker = state.ticker
  val isInWatchlist = state.isInWatchlist
  val isAllowedToModifyWatchlist = state.isAllowModifyWatchlist
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
      actions = {
        if (!isLoading && isAllowedToModifyWatchlist) {
          IconButton(
              onClick = onModifyWatchlist,
          ) {
            Icon(
                imageVector = if (isInWatchlist) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "${if (isInWatchlist) "Add to" else "Remove from"} Watchlist",
            )
          }
        }
      })
}

@Preview
@Composable
private fun PreviewWatchlistDigToolbar() {
  val symbol = "MSFT".asSymbol()
  WatchlistDigToolbar(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
              allowModifyWatchlist = true,
          ),
      onClose = {},
      onModifyWatchlist = {},
  )
}
