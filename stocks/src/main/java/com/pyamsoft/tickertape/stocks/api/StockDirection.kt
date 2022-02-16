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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import com.pyamsoft.tickertape.stocks.data.StockDirectionImpl

interface StockDirection : StockNumberValue {

  @CheckResult fun sign(): String

  @CheckResult fun isUp(): Boolean

  @CheckResult fun isDown(): Boolean

  @ColorInt @CheckResult fun color(): Int

  companion object {

    private val EMPTY = StockDirectionImpl(0.0)
    private val UP = StockDirectionImpl(1.0)
    private val DOWN = StockDirectionImpl(-1.0)

    @JvmStatic
    @CheckResult
    fun none(): StockDirection {
      return EMPTY
    }

    @JvmStatic
    @CheckResult
    fun up(): StockDirection {
      return UP
    }

    @JvmStatic
    @CheckResult
    fun down(): StockDirection {
      return DOWN
    }
  }
}

@CheckResult
fun Double.asDirection(): StockDirection {
  val comparison = this.compareTo(0)
  return when {
    comparison == 0 -> StockDirection.none()
    comparison > 0 -> StockDirection.up()
    else -> StockDirection.down()
  }
}
