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

import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import java.time.LocalDate
import java.time.LocalDateTime

internal data class StockOptionsImpl
internal constructor(
    override val symbol: StockSymbol,
    override val expirationDates: List<LocalDate>,
    override val strikes: List<StockMoneyValue>,
    override val date: LocalDate,
    override val calls: List<StockOptions.Call>,
    override val puts: List<StockOptions.Put>
) : StockOptions {

  internal data class ContractImpl
  internal constructor(
      override val type: StockOptions.Contract.Type,
      override val symbol: StockSymbol,
      override val contractSymbol: StockSymbol,
      override val strike: StockMoneyValue,
      override val change: StockMoneyValue,
      override val percent: StockPercent,
      override val lastPrice: StockMoneyValue,
      override val iv: StockPercent,
      override val itm: Boolean,
      override val lastTradeDate: LocalDateTime,
      override val expirationDate: LocalDate,
      override val openInterest: Int,
      override val bid: StockMoneyValue,
      override val ask: StockMoneyValue,
  ) : StockOptions.Contract, StockOptions.Call, StockOptions.Put {

    override val mid: StockMoneyValue =
        if (bid == StockMoneyValue.NONE || ask == StockMoneyValue.NONE) {
          StockMoneyValue.NONE
        } else {
          val bidValue = bid.value
          val diff = ask.value - bidValue
          (bidValue + diff).asMoney()
        }
  }
}
