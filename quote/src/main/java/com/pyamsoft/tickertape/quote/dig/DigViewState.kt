package com.pyamsoft.tickertape.quote.dig

import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import java.time.LocalDateTime

interface DigViewState : UiViewState {
    val ticker: Ticker
    val range: StockChart.IntervalRange
    val currentDate: LocalDateTime
    val currentPrice: StockMoneyValue?
}
