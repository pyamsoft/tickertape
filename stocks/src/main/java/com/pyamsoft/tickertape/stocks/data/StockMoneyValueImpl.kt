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
import com.pyamsoft.tickertape.stocks.api.BIG_MONEY_FORMATTER
import com.pyamsoft.tickertape.stocks.api.SMALL_MONEY_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import kotlin.math.abs

internal data class StockMoneyValueImpl(
    override val value: Double,
) : StockMoneyValue {

  private val money by
      lazy(LazyThreadSafetyMode.NONE) {
        // If its a small money < $10, allow more decimals
        val formatter =
            if (abs(value).compareTo(10) < 0) SMALL_MONEY_FORMATTER else BIG_MONEY_FORMATTER
        return@lazy if (isZero) ZERO_VAL else formatter.get().requireNotNull().format(value)
      }

  override val isZero: Boolean = value.isZero()

  override val display: String = money

  override fun compareTo(other: StockMoneyValue): Int {
    return this.value.compareTo(other.value)
  }

  companion object {

    private const val ZERO_VAL = "$0.000"
  }
}
