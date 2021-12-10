package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

interface WatchlistDigViewState : UiViewState {
  val isLoading: Boolean
  val ticker: Ticker
  val range: StockChart.IntervalRange
  val error: Throwable?
}

internal class MutableWatchlistDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
) : WatchlistDigViewState {
  override var isLoading by mutableStateOf(false)
  override var error by mutableStateOf<Throwable?>(null)
  override var range by mutableStateOf(StockChart.IntervalRange.ONE_DAY)
  override var ticker by
      mutableStateOf(
          Ticker(
              symbol = symbol,
              quote = null,
              chart = null,
          ),
      )
}
