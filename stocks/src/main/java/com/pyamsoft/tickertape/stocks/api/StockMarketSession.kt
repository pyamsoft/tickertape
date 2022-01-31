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
import com.pyamsoft.tickertape.stocks.data.StockMarketSessionImpl

interface StockMarketSession {

  @CheckResult fun direction(): StockDirection

  @CheckResult fun price(): StockMoneyValue

  @CheckResult fun amount(): StockMoneyValue

  @CheckResult fun percent(): StockPercent

  @CheckResult fun state(): MarketState

  companion object {
    @JvmStatic
    @CheckResult
    fun create(
        direction: StockDirection,
        price: StockMoneyValue,
        amount: StockMoneyValue,
        percent: StockPercent,
        state: MarketState,
    ): StockMarketSession {
      return StockMarketSessionImpl(
          direction = direction,
          price = price,
          amount = amount,
          percent = percent,
          state = state,
      )
    }
  }
}
