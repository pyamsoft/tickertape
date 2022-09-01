package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

@Stable
interface WatchlistDigViewState : DigViewState {
  val section: WatchlistDigSections

  val isInWatchlist: Boolean
  val isInWatchlistError: Throwable?
}

// Public for WatchlistDigViewModeler constructor
@Stable
class MutableWatchlistDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
) : MutableDigViewState(symbol), WatchlistDigViewState {

  override var section by mutableStateOf(WatchlistDigSections.CHART)

  override var isInWatchlist by mutableStateOf(false)
  override var isInWatchlistError by mutableStateOf<Throwable?>(null)
}
