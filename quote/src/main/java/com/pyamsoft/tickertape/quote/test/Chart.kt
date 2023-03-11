/*
 * Copyright 2021 Peter Kenji Yamanaka
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
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.Clock
import java.time.LocalDateTime

/** Should only be used in tests/preview */
@CheckResult
fun newTestChart(
    symbol: StockSymbol,
    clock: Clock,
): StockChart {
  return object : StockChart {
    override val symbol: StockSymbol = symbol
    override val range: StockChart.IntervalRange = StockChart.IntervalRange.ONE_DAY
    override val interval: StockChart.IntervalTime = StockChart.IntervalTime.ONE_DAY
    override val startingPrice: StockMoneyValue = StockMoneyValue.NONE
    override val currentPrice: StockMoneyValue = StockMoneyValue.NONE
    override val currentDate: LocalDateTime = LocalDateTime.now(clock)
    override val dates: List<LocalDateTime> = emptyList()
    override val close: List<StockMoneyValue> = emptyList()
  }
}
