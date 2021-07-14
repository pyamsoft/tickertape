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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

internal data class StockChartImpl(
    private val symbol: StockSymbol,
    private val range: StockChart.IntervalRange,
    private val interval: StockChart.IntervalTime,
    private val dates: List<LocalDateTime>,
    private val currentPrice: StockMoneyValue,
    private val startingPrice: StockMoneyValue,
    private val currentDate: LocalDateTime,
    private val close: List<StockMoneyValue>,
) : StockChart {

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun range(): StockChart.IntervalRange {
    return range
  }

  override fun interval(): StockChart.IntervalTime {
    return interval
  }

  override fun dates(): List<LocalDateTime> {
    return dates
  }

  override fun startingPrice(): StockMoneyValue {
    return startingPrice
  }

  override fun currentPrice(): StockMoneyValue {
    return currentPrice
  }

  override fun currentDate(): LocalDateTime {
    return currentDate
  }

  override fun close(): List<StockMoneyValue> {
    return close
  }
}
