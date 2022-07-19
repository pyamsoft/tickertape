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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asMoney
import java.time.LocalDateTime

internal data class StockOptionsImpl
internal constructor(
    private val symbol: StockSymbol,
    private val expirationDates: List<LocalDateTime>,
    private val strikes: List<StockMoneyValue>,
    private val date: LocalDateTime,
    private val calls: List<StockOptions.Call>,
    private val puts: List<StockOptions.Put>
) : StockOptions {

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun expirationDates(): List<LocalDateTime> {
    return expirationDates
  }

  override fun strikes(): List<StockMoneyValue> {
    return strikes
  }

  override fun date(): LocalDateTime {
    return date
  }

  override fun calls(): List<StockOptions.Call> {
    return calls
  }

  override fun puts(): List<StockOptions.Put> {
    return puts
  }

  internal data class ContractImpl
  internal constructor(
      private val type: StockOptions.Contract.Type,
      private val symbol: StockSymbol,
      private val contractSymbol: StockSymbol,
      private val strike: StockMoneyValue,
      private val change: StockMoneyValue,
      private val percent: StockPercent,
      private val lastPrice: StockMoneyValue,
      private val bid: StockMoneyValue,
      private val ask: StockMoneyValue,
      private val iv: StockPercent,
      private val itm: Boolean,
  ) : StockOptions.Contract, StockOptions.Call, StockOptions.Put {

    private val mid: StockMoneyValue

    init {
      val bidValue = bid.value
      val diff = ask.value - bidValue
      mid = (bidValue + diff).asMoney()
    }

    override fun type(): StockOptions.Contract.Type {
      return type
    }

    override fun symbol(): StockSymbol {
      return symbol
    }

    override fun contractSymbol(): StockSymbol {
      return contractSymbol
    }

    override fun strike(): StockMoneyValue {
      return strike
    }

    override fun change(): StockMoneyValue {
      return change
    }

    override fun percent(): StockPercent {
      return percent
    }

    override fun lastPrice(): StockMoneyValue {
      return lastPrice
    }

    override fun bid(): StockMoneyValue {
      return bid
    }

    override fun ask(): StockMoneyValue {
      return ask
    }

    override fun mid(): StockMoneyValue {
      return mid
    }

    override fun iv(): StockPercent {
      return iv
    }

    override fun itm(): Boolean {
      return itm
    }
  }
}
