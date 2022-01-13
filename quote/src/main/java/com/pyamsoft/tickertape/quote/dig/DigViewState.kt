package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

interface DigViewState : UiViewState {
  val ticker: Ticker
  val range: StockChart.IntervalRange
  val currentDate: LocalDateTime
  val currentPrice: StockMoneyValue?
  val mostRecentPrice: StockMoneyValue?
}

abstract class MutableDigViewState
protected constructor(
    symbol: StockSymbol,
) : DigViewState {
  final override var range by mutableStateOf(StockChart.IntervalRange.ONE_DAY)
  final override var currentDate by mutableStateOf<LocalDateTime>(LocalDateTime.now())
  final override var currentPrice by mutableStateOf<StockMoneyValue?>(null)
  final override var mostRecentPrice by mutableStateOf<StockMoneyValue?>(null)
  final override var ticker by
      mutableStateOf(
          Ticker(
              symbol = symbol,
              quote = null,
              chart = null,
          ),
      )
}
