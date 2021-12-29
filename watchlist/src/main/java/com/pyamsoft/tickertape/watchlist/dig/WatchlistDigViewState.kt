package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

interface WatchlistDigViewState : DigViewState {
  val isInWatchlist: Boolean
  val isAllowModifyWatchlist: Boolean
  val isLoading: Boolean
  val error: Throwable?
}

// Public for WatchlistDigViewModeler constructor
class MutableWatchlistDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
    allowModifyWatchlist: Boolean,
) : MutableDigViewState(symbol), WatchlistDigViewState {

  // Not state backed since this is constant
  override val isAllowModifyWatchlist = allowModifyWatchlist

  override var isInWatchlist by mutableStateOf(false)
  override var isLoading by mutableStateOf(false)
  override var error by mutableStateOf<Throwable?>(null)
}
