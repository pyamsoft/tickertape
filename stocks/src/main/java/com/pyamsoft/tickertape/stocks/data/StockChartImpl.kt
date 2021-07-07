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
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import java.time.LocalDateTime

internal data class StockChartImpl(
    private val symbol: StockSymbol,
    private val range: StockChart.IntervalRange,
    private val interval: StockChart.IntervalTime,
    private val dates: List<LocalDateTime>,
    private val startingPrice: StockMoneyValue,
    private val volume: List<StockVolumeValue>,
    private val open: List<StockMoneyValue>,
    private val close: List<StockMoneyValue>,
    private val high: List<StockMoneyValue>,
    private val low: List<StockMoneyValue>,
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

  override fun volume(): List<StockVolumeValue> {
    return volume
  }

  override fun open(): List<StockMoneyValue> {
    return open
  }

  override fun close(): List<StockMoneyValue> {
    return close
  }

  override fun high(): List<StockMoneyValue> {
    return high
  }

  override fun low(): List<StockMoneyValue> {
    return low
  }
}
