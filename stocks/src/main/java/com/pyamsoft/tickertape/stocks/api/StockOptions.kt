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
  }

  interface Call : Contract
  interface Put : Contract
}
