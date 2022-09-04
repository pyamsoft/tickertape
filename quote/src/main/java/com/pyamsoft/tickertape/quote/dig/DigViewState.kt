package com.pyamsoft.tickertape.quote.dig

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDate
import java.time.LocalDateTime

@Stable
interface DigViewState : UiViewState {
  val isLoading: Boolean

  val ticker: Ticker

  val range: StockChart.IntervalRange
  val currentDate: LocalDateTime
  val currentPrice: StockMoneyValue?
  val openingPrice: StockMoneyValue?
  val chartError: Throwable?

  val news: List<StockNews>
  val newsError: Throwable?

  val statistics: KeyStatistics?
  val statisticsError: Throwable?

  val recommendations: List<Ticker>
  val recommendationError: Throwable?

  val optionsChain: StockOptions?
  val optionsError: Throwable?
  val optionsSection: StockOptions.Contract.Type
  val optionsExpirationDate: LocalDate?
}

@Stable
abstract class MutableDigViewState
protected constructor(
    symbol: StockSymbol,
) : DigViewState {
  final override var isLoading by mutableStateOf(false)

  final override var optionsChain by mutableStateOf<StockOptions?>(null)
  final override var optionsError by mutableStateOf<Throwable?>(null)
  final override var optionsSection by mutableStateOf(StockOptions.Contract.Type.CALL)
  final override var optionsExpirationDate by mutableStateOf<LocalDate?>(null)

  final override var statistics by mutableStateOf<KeyStatistics?>(null)
  final override var statisticsError by mutableStateOf<Throwable?>(null)

  final override var recommendations by mutableStateOf(emptyList<Ticker>())
  final override var recommendationError by mutableStateOf<Throwable?>(null)

  final override var news by mutableStateOf(emptyList<StockNews>())
  final override var newsError by mutableStateOf<Throwable?>(null)

  final override var chartError by mutableStateOf<Throwable?>(null)
  final override var range by mutableStateOf(StockChart.IntervalRange.ONE_DAY)
  final override var currentDate by mutableStateOf<LocalDateTime>(LocalDateTime.now())
  final override var currentPrice by mutableStateOf<StockMoneyValue?>(null)
  final override var openingPrice by mutableStateOf<StockMoneyValue?>(null)

  final override var ticker by
      mutableStateOf(
          Ticker(
              symbol = symbol,
              quote = null,
              chart = null,
          ),
      )
}
