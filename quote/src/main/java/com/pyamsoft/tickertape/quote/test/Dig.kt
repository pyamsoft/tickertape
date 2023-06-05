/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.test

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.chart.ChartDataPainter
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.recommend.StockRec
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.ui.test.TestClock
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Should only be used in tests/preview */
@CheckResult
fun newTestDigViewState(
    symbol: StockSymbol = TestSymbol,
    clock: Clock = TestClock,
): DigViewState {
  return object : DigViewState {
    override val range: StateFlow<StockChart.IntervalRange> =
        MutableStateFlow(StockChart.IntervalRange.ONE_DAY)
    override val currentDate: StateFlow<LocalDateTime> = MutableStateFlow(LocalDateTime.now(clock))
    override val currentPrice: StateFlow<StockMoneyValue?> = MutableStateFlow(420.69.asMoney())
    override val openingPrice: StateFlow<StockMoneyValue?> = MutableStateFlow(69.420.asMoney())
    override val chartError: StateFlow<Throwable?> = MutableStateFlow(null)
    override val loadingState: StateFlow<BaseDigViewState.LoadingState> =
        MutableStateFlow(BaseDigViewState.LoadingState.NONE)
    override val ticker =
        MutableStateFlow(
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = newTestChart(symbol, clock),
            ))
    override val news: StateFlow<List<StockNews>> = MutableStateFlow(emptyList())
    override val newsError: StateFlow<Throwable?> = MutableStateFlow(null)
    override val statistics: StateFlow<KeyStatistics?> = MutableStateFlow(null)
    override val statisticsError: StateFlow<Throwable?> = MutableStateFlow(null)
    override val recommendations: StateFlow<List<StockRec>> = MutableStateFlow(emptyList())
    override val recommendationError: StateFlow<Throwable?> = MutableStateFlow(null)
    override val optionsChain: StateFlow<StockOptions?> = MutableStateFlow(null)
    override val optionsError: StateFlow<Throwable?> = MutableStateFlow(null)
    override val optionsSection: StateFlow<StockOptions.Contract.Type> =
        MutableStateFlow(StockOptions.Contract.Type.CALL)
    override val optionsExpirationDate: StateFlow<LocalDate?> = MutableStateFlow(null)
    override val priceAlerts: StateFlow<List<PriceAlert>> = MutableStateFlow(emptyList())
    override val chart: StateFlow<ChartDataPainter?> = MutableStateFlow(null)
  }
}
