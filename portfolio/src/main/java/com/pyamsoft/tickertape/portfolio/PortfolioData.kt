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

package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent

@Stable
@Immutable
data class PortfolioData(
    val stocks: Data,
    val options: Data,
    val crypto: Data,
) {

  @Stable
  @Immutable
  data class Data(
      val current: StockMoneyValue,
      val total: Summary,
      val today: Summary,
      val positions: Positions,
  ) {

    @Stable
    @Immutable
    data class Summary(
        val change: StockMoneyValue,
        val changePercent: StockPercent,
        val direction: StockDirection,
    )

    @Stable
    @Immutable
    data class Positions(
        val shortTerm: Int,
        val longTerm: Int,
    ) {
      @CheckResult
      fun isEmpty(): Boolean {
        return shortTerm <= 0 && longTerm <= 0
      }
    }
  }

  companion object {

    private val EMPTY_SUMMARY =
        Data.Summary(
            change = StockMoneyValue.NONE,
            changePercent = StockPercent.NONE,
            direction = StockDirection.NONE,
        )

    private val EMPTY_POSITIONS =
        Data.Positions(
            shortTerm = 0,
            longTerm = 0,
        )

    private val EMPTY_DATA =
        Data(
            current = StockMoneyValue.NONE,
            total = EMPTY_SUMMARY,
            today = EMPTY_SUMMARY,
            positions = EMPTY_POSITIONS,
        )

    @JvmField
    val EMPTY =
        PortfolioData(
            stocks = EMPTY_DATA,
            options = EMPTY_DATA,
            crypto = EMPTY_DATA,
        )
  }
}
