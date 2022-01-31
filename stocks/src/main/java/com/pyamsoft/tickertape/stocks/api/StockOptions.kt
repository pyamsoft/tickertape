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

  @CheckResult fun symbol(): StockSymbol

  @CheckResult fun expirationDates(): List<LocalDateTime>

  @CheckResult fun strikes(): List<StockMoneyValue>

  @CheckResult fun date(): LocalDateTime

  @CheckResult fun calls(): List<Call>

  @CheckResult fun puts(): List<Put>

  interface Contract {

    @CheckResult fun type(): Type

    @CheckResult fun symbol(): StockSymbol

    @CheckResult fun contractSymbol(): StockSymbol

    @CheckResult fun strike(): StockMoneyValue

    @CheckResult fun change(): StockMoneyValue

    @CheckResult fun percent(): StockPercent

    @CheckResult fun lastPrice(): StockMoneyValue

    @CheckResult fun bid(): StockMoneyValue

    @CheckResult fun ask(): StockMoneyValue

    @CheckResult fun mid(): StockMoneyValue

    @CheckResult fun iv(): StockPercent

    @CheckResult fun itm(): Boolean

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
            type = type,
            symbol = symbol,
            contractSymbol = contractSymbol,
            strike = strike,
            change = change,
            percent = percent,
            lastPrice = lastPrice,
            bid = bid,
            ask = ask,
            iv = iv,
            itm = itm,
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
            type = type,
            symbol = symbol,
            contractSymbol = contractSymbol,
            strike = strike,
            change = change,
            percent = percent,
            lastPrice = lastPrice,
            bid = bid,
            ask = ask,
            iv = iv,
            itm = itm,
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
          symbol = symbol,
          expirationDates = expirationDates,
          strikes = strikes,
          date = date,
          calls = calls,
          puts = puts,
      )
    }
  }
}
