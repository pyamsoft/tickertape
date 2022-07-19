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
import com.pyamsoft.tickertape.stocks.data.StockOptionsImpl
import java.time.LocalDateTime

interface StockOptions {

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val expirationDates: List<LocalDateTime>

  @get:CheckResult val strikes: List<StockMoneyValue>

  @get:CheckResult val date: LocalDateTime

  @get:CheckResult val calls: List<Call>

  @get:CheckResult val puts: List<Put>

  interface Contract {

    @get:CheckResult val type: Type

    @get:CheckResult val symbol: StockSymbol

    @get:CheckResult val contractSymbol: StockSymbol

    @get:CheckResult val strike: StockMoneyValue

    @get:CheckResult val change: StockMoneyValue

    @get:CheckResult val percent: StockPercent

    @get:CheckResult val lastPrice: StockMoneyValue

    @get:CheckResult val bid: StockMoneyValue

    @get:CheckResult val ask: StockMoneyValue

    @get:CheckResult val mid: StockMoneyValue

    @get:CheckResult val iv: StockPercent

    @get:CheckResult val itm: Boolean

    enum class Type {
      CALL,
      PUT
    }

    companion object {

      @CheckResult
      private fun createContract(
          type: Type,
          symbol: StockSymbol,
          contractSymbol: StockSymbol,
          strike: StockMoneyValue,
          change: StockMoneyValue,
          percent: StockPercent,
          lastPrice: StockMoneyValue,
          bid: StockMoneyValue,
          ask: StockMoneyValue,
          iv: StockPercent,
          itm: Boolean,
      ): StockOptionsImpl.ContractImpl {
        return StockOptionsImpl.ContractImpl(
            type,
            symbol,
            contractSymbol,
            strike,
            change,
            percent,
            lastPrice,
            bid,
            ask,
            iv,
            itm,
        )
      }

      @JvmStatic
      @CheckResult
      fun <T : Contract> create(
          type: Type,
          symbol: StockSymbol,
          contractSymbol: StockSymbol,
          strike: StockMoneyValue,
          change: StockMoneyValue,
          percent: StockPercent,
          lastPrice: StockMoneyValue,
          bid: StockMoneyValue,
          ask: StockMoneyValue,
          iv: StockPercent,
          itm: Boolean,
      ): T {
        @Suppress("UNCHECKED_CAST")
        return createContract(
            type,
            symbol,
            contractSymbol,
            strike,
            change,
            percent,
            lastPrice,
            bid,
            ask,
            iv,
            itm,
        ) as
            T
      }
    }
  }

  interface Call : Contract
  interface Put : Contract

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        expirationDates: List<LocalDateTime>,
        strikes: List<StockMoneyValue>,
        date: LocalDateTime,
        calls: List<Call>,
        puts: List<Put>
    ): StockOptions {
      return StockOptionsImpl(
          symbol,
          expirationDates,
          strikes,
          date,
          calls,
          puts,
      )
    }
  }
}
