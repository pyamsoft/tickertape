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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.data.StockOptionsImpl
import java.time.LocalDate
import java.time.LocalDateTime

@Stable
interface StockOptions {

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val expirationDates: List<LocalDate>

  @get:CheckResult val strikes: List<StockMoneyValue>

  @get:CheckResult val date: LocalDate

  @get:CheckResult val calls: List<Call>

  @get:CheckResult val puts: List<Put>

  @Stable
  interface Contract {

    @get:CheckResult val type: Type

    @get:CheckResult val symbol: StockSymbol

    @get:CheckResult val contractSymbol: StockSymbol

    @get:CheckResult val strike: StockMoneyValue

    @get:CheckResult val change: StockMoneyValue

    @get:CheckResult val percent: StockPercent

    @get:CheckResult val lastPrice: StockMoneyValue

    @get:CheckResult val iv: StockPercent

    @get:CheckResult val itm: Boolean

    @get:CheckResult val openInterest: Int

    @get:CheckResult val expirationDate: LocalDate

    @get:CheckResult val lastTradeDate: LocalDateTime

    @get:CheckResult val mid: StockMoneyValue

    @get:CheckResult val bid: StockMoneyValue

    @get:CheckResult val ask: StockMoneyValue

    @Stable
    @Immutable
    enum class Type(val display: String) {
      CALL("Calls"),
      PUT("Puts")
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
          iv: StockPercent,
          itm: Boolean,
          lastTradeDate: LocalDateTime,
          expirationDate: LocalDate,
          openInterest: Int,
          bid: StockMoneyValue,
          ask: StockMoneyValue,
      ): StockOptionsImpl.ContractImpl {
        return StockOptionsImpl.ContractImpl(
            type,
            symbol,
            contractSymbol,
            strike,
            change,
            percent,
            lastPrice,
            iv,
            itm,
            lastTradeDate,
            expirationDate,
            openInterest,
            bid,
            ask,
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
          iv: StockPercent,
          itm: Boolean,
          lastTradeDate: LocalDateTime,
          expirationDate: LocalDate,
          openInterest: Int,
          bid: StockMoneyValue,
          ask: StockMoneyValue,
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
            iv,
            itm,
            lastTradeDate,
            expirationDate,
            openInterest,
            bid,
            ask,
        )
            as T
      }
    }
  }

  @Stable interface Call : Contract

  @Stable interface Put : Contract

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        expirationDates: List<LocalDate>,
        strikes: List<StockMoneyValue>,
        date: LocalDate,
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
