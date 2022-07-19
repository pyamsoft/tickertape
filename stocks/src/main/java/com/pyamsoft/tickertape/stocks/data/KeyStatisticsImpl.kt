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

import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockSymbol

internal data class KeyStatisticsImpl(
    private val symbol: StockSymbol,
    private val earnings: KeyStatistics.Earnings,
    private val financials: KeyStatistics.Financials,
    private val info: KeyStatistics.Info,
) : KeyStatistics {

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun earnings(): KeyStatistics.Earnings {
    return earnings
  }

  override fun financials(): KeyStatistics.Financials {
    return financials
  }

  override fun info(): KeyStatistics.Info {
    return info
  }
}
