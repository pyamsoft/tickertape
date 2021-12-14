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

import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.PERCENT_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockPercent

internal data class StockPercentImpl(private val percent: Double) : StockPercent {

  private val stockPercent by
      lazy(LazyThreadSafetyMode.NONE) {
        if (isZero()) "0.00%" else PERCENT_FORMATTER.get().requireNotNull().format(percent / 100)
      }

  override fun asPercentValue(): String {
    return stockPercent
  }

  override fun value(): Double {
    return percent
  }

  override fun isZero(): Boolean {
    return percent.isZero()
  }
}
