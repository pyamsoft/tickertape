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

interface StockMarketSession {

  @CheckResult fun direction(): StockDirection

  @CheckResult fun price(): StockMoneyValue

  @CheckResult fun amount(): StockMoneyValue

  @CheckResult fun percent(): StockPercent

  data class SessionData
  internal constructor(
      val percent: String,
      val changeAmount: String,
      val directionSign: String,
      @ColorInt val color: Int
  )

  companion object {

    @JvmStatic
    @CheckResult
    fun getDataFromSession(session: StockMarketSession): SessionData {
      val percent: String
      val changeAmount: String
      val directionSign: String
      val direction = session.direction()
      if (direction.isZero()) {
        directionSign = ""
        percent = "0.00%"
        changeAmount = "0.00"
      } else {
        percent = session.percent().asPercentValue()
        changeAmount = session.amount().asFixedValue()
        // Direction sign not needed for negative numbers
        directionSign = if (direction.isUp()) "+" else ""
      }

      return SessionData(percent, changeAmount, directionSign, direction.color())
    }
  }
}
