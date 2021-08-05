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

package com.pyamsoft.tickertape.db.mover

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class JsonMappableBigMoverReport
internal constructor(
    internal val id: BigMoverReport.Id,
    internal val symbol: StockSymbol,
    internal val lastState: MarketState,
    internal val lastNotified: LocalDateTime,
    internal val lastPercent: StockPercent,
    internal val lastPrice: StockMoneyValue,
) : BigMoverReport {

  override fun id(): BigMoverReport.Id {
    return id
  }

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun lastNotified(): LocalDateTime {
    return lastNotified
  }

  override fun lastState(): MarketState {
    return lastState
  }

  override fun lastPercent(): StockPercent {
    return lastPercent
  }

  override fun lastPrice(): StockMoneyValue {
    return lastPrice
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(quote: StockQuote): BigMoverReport {
      val price: StockMoneyValue
      val percent: StockPercent
      val state: MarketState

      val afterHours = quote.afterHours()
      val preMarket = quote.preMarket()
      val regular = quote.regular()
      when {
        afterHours != null -> {
          state = MarketState.POST
          price = afterHours.price()
          percent = afterHours.percent()
        }
        preMarket != null -> {
          state = MarketState.PRE
          price = preMarket.price()
          percent = preMarket.percent()
        }
        else -> {
          state = MarketState.REGULAR
          price = regular.price()
          percent = regular.percent()
        }
      }

      return JsonMappableBigMoverReport(
          id = BigMoverReport.Id(IdGenerator.generate()),
          symbol = quote.symbol(),
          lastPrice = price,
          lastPercent = percent,
          lastState = state,
          lastNotified = LocalDateTime.now())
    }

    @JvmStatic
    @CheckResult
    fun from(item: BigMoverReport): JsonMappableBigMoverReport {
      return if (item is JsonMappableBigMoverReport) item
      else {
        JsonMappableBigMoverReport(
            item.id(),
            item.symbol(),
            item.lastState(),
            item.lastNotified(),
            item.lastPercent(),
            item.lastPrice(),
        )
      }
    }
  }
}
