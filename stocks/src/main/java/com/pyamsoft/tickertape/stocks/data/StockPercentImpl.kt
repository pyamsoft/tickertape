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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.PERCENT_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockPercent
import kotlin.math.abs

internal data class StockPercentImpl(
    override val value: Double,
    override val isValid: Boolean,
    private val isPercentageOutOfHundred: Boolean,
) : StockPercent {

  private val stockPercent by
      lazy(LazyThreadSafetyMode.NONE) {
        // Use abs() because the sign will be determined by StockDirection
        val v = abs(value)
        // If this percentage is out of 100, like 1.5% being 1.53 instead of 0.0153, divide by 100
        val divisor = if (isPercentageOutOfHundred) 100 else 1

        return@lazy if (isZero) "0.00%"
        else {
          val formatter = PERCENT_FORMATTER.get().requireNotNull()
          formatter.format(v / divisor)
        }
      }

  override val isZero: Boolean = value.isZero()

  override val display: String = stockPercent

  override fun compareTo(other: StockPercent): Int {
    return this.value.compareTo(other.value)
  }
}
