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

import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.core.isNegative
import com.pyamsoft.tickertape.core.isPositive
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.StockDirection

internal data class StockDirectionImpl(private val price: Double) : StockDirection {

  override fun sign(): String {
    return when {
      isUp() -> "+"
      // Direction sign not needed for negative numbers
      isDown() -> ""
      isZero() -> ""
      else -> ""
    }
  }

  override fun isUp(): Boolean {
    return price.isPositive()
  }

  override fun isDown(): Boolean {
    return price.isNegative()
  }

  override fun isZero(): Boolean {
    return price.isZero()
  }

  override fun color(): Int {
    return when {
      isUp() -> DEFAULT_STOCK_UP_COLOR
      isDown() -> DEFAULT_STOCK_DOWN_COLOR
      isZero() -> DEFAULT_STOCK_COLOR
      else -> DEFAULT_STOCK_COLOR
    }
  }
}
