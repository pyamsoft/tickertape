/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.data.StockDirectionImpl

@Stable
interface StockDirection : StockNumberValue {

  @get:CheckResult val sign: String

  @get:CheckResult val isUp: Boolean

  @get:CheckResult val isDown: Boolean

  @get:[ColorInt CheckResult]
  val color: Int

  companion object {

    val NONE: StockDirection =
        StockDirectionImpl(
            price = 0.0,
            isValid = false,
        )
    val UP: StockDirection =
        StockDirectionImpl(
            price = 1.0,
            isValid = true,
        )
    val DOWN: StockDirection =
        StockDirectionImpl(
            price = -1.0,
            isValid = true,
        )
  }
}

@CheckResult
fun StockDirection.flip(): StockDirection {
  return when {
    this.isUp -> StockDirection.DOWN
    this.isDown -> StockDirection.UP
    else -> StockDirection.NONE
  }
}

@CheckResult
fun StockDirection.asGainLoss(): String {
  return when {
    this.isUp -> "Gain"
    this.isDown -> "Loss"
    else -> "Change"
  }
}

@CheckResult
fun Double.asDirection(): StockDirection {
  val comparison = this.compareTo(0)
  return when {
    comparison < 0 -> StockDirection.DOWN
    comparison > 0 -> StockDirection.UP
    else -> StockDirection.NONE
  }
}
