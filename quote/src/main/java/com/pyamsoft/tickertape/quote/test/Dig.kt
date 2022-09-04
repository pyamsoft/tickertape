package com.pyamsoft.tickertape.quote.test

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDateTime

/** Should only be used in tests/preview */
@CheckResult
fun newTestDigViewState(symbol: StockSymbol = "MSFT".asSymbol()): DigViewState {
  return object : DigViewState {
    override val ticker =
        Ticker(
            symbol = symbol,
            quote = newTestQuote(symbol),
            chart = newTestChart(symbol),
        )

    override val isLoading: Boolean = false

    override val recommendationError: Throwable? = null

    override val recommendations: List<Ticker> = emptyList()

    override val range = StockChart.IntervalRange.ONE_DAY

    override val currentDate = LocalDateTime.now()

    override val currentPrice = 1.0.asMoney()

    override val openingPrice = currentPrice

    override val chartError: Throwable? = null

    override val news = emptyList<StockNews>()

    override val newsError: Throwable? = null

    override val statistics: KeyStatistics? = null

    override val statisticsError: Throwable? = null

    override val optionsChain: StockOptions? = null

    override val optionsError: Throwable? = null

    override val optionsExpirationDate: LocalDateTime? = null

    override val optionsSection: StockOptions.Contract.Type = StockOptions.Contract.Type.CALL
  }
}
