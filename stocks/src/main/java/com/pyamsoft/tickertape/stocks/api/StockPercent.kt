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
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.data.StockPercentImpl

@Stable
interface StockPercent : StockDoubleValue {

  @get:CheckResult val display: String

  @CheckResult fun compareTo(other: StockPercent): Int

  companion object {

    val NONE: StockPercent =
        StockPercentImpl(
            value = 0.0,
            isValid = false,
            isPercentageOutOfHundred = false,
        )
  }
}

/**
 * Treat double as percent
 *
 * @param isPercentageOutOfHundred If this percentage is out of 100, like 1.5% being 1.53 instead of
 *   0.0153, divide by 100
 */
@CheckResult
fun Double.asPercent(isPercentageOutOfHundred: Boolean = true): StockPercent {
  return StockPercentImpl(
      value = this,
      isValid = true,
      isPercentageOutOfHundred = isPercentageOutOfHundred,
  )
}
