package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

interface WatchlistDigViewState : DigViewState {
  val isLoading: Boolean
  val error: Throwable?
}

// Public for WatchlistDigViewModeler constructor
class MutableWatchlistDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
) : MutableDigViewState(symbol), WatchlistDigViewState {
  override var isLoading by mutableStateOf(false)
  override var error by mutableStateOf<Throwable?>(null)
}