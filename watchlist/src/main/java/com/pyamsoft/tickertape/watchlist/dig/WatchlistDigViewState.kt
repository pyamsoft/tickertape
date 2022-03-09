package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Named

interface WatchlistDigViewState : DigViewState {
  val section: WatchlistDigSections
  val isInWatchlist: Boolean
  val isAllowModifyWatchlist: Boolean
  val isLoading: Boolean

  val isInWatchlistError: Throwable?
}

// Public for WatchlistDigViewModeler constructor
class MutableWatchlistDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
    @Named("lookup") lookupSymbol: StockSymbol,
    allowModifyWatchlist: Boolean,
    equityType: EquityType,
) : MutableDigViewState(symbol, lookupSymbol, equityType), WatchlistDigViewState {

  // Not state backed since this is constant
  override val isAllowModifyWatchlist = allowModifyWatchlist

  override var section by mutableStateOf(WatchlistDigSections.CHART)
  override var isLoading by mutableStateOf(false)

  override var isInWatchlist by mutableStateOf(false)
  override var isInWatchlistError by mutableStateOf<Throwable?>(null)
}
