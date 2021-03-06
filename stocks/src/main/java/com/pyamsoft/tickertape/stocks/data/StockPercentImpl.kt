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

internal data class StockPercentImpl(
    override val value: Double,
) : StockPercent {

  private val stockPercent by
      lazy(LazyThreadSafetyMode.NONE) {
        if (isZero) "0.00%" else PERCENT_FORMATTER.get().requireNotNull().format(value / 100)
      }

  override val isZero: Boolean = value.isZero()

  override val display: String = stockPercent

  override fun compareTo(other: StockPercent): Int {
    return this.value.compareTo(other.value)
  }
}
