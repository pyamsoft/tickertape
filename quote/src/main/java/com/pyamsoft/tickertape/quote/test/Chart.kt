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
import com.pyamsoft.tickertape.stocks.api.asMoney
import java.time.LocalDateTime

/** Should only be used in tests/preview */
@CheckResult
fun newTestChart(symbol: StockSymbol): StockChart {
  return object : StockChart {
    override fun symbol(): StockSymbol {
      return symbol
    }

    override fun range(): StockChart.IntervalRange {
      return StockChart.IntervalRange.ONE_DAY
    }

    override fun interval(): StockChart.IntervalTime {
      return StockChart.IntervalTime.ONE_DAY
    }

    override fun startingPrice(): StockMoneyValue {
      return 0.5.asMoney()
    }

    override fun currentPrice(): StockMoneyValue {
      return 1.0.asMoney()
    }

    override fun currentDate(): LocalDateTime {
      return LocalDateTime.now()
    }

    override fun dates(): List<LocalDateTime> {
      return listOf(currentDate().minusDays(2), currentDate().minusDays(1), currentDate())
    }

    override fun close(): List<StockMoneyValue> {
      return listOf(
          0.7.asMoney(),
          0.2.asMoney(),
          1.0.asMoney(),
      )
    }
  }
}
