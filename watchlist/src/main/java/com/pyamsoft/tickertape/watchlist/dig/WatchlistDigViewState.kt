package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime
import javax.inject.Inject

interface WatchlistDigViewState : DigViewState {
  val isLoading: Boolean
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
  override var currentDate by mutableStateOf<LocalDateTime>(LocalDateTime.now())
  override var currentPrice by mutableStateOf<StockMoneyValue?>(null)
  override var ticker by
      mutableStateOf(
          Ticker(
              symbol = symbol,
              quote = null,
              chart = null,
          ),
      )
}
