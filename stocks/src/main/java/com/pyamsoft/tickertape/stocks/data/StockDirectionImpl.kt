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

import android.graphics.Color
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_COLOR
import com.pyamsoft.tickertape.stocks.api.StockDirection

internal data class StockDirectionImpl(private val price: Double) : StockDirection {

  override fun isUp(): Boolean {
    return price.compareTo(0) > 0
  }

  override fun isDown(): Boolean {
    return price.compareTo(0) < 0
  }

  override fun isZero(): Boolean {
    return price.compareTo(0) == 0
  }

  override fun color(): Int {
    return when {
      isUp() -> Color.GREEN
      isDown() -> Color.RED
      isZero() -> DEFAULT_STOCK_COLOR
      else -> DEFAULT_STOCK_COLOR
    }
  }
}
