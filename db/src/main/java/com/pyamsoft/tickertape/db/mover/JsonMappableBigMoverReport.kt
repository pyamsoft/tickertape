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

package com.pyamsoft.tickertape.db.mover

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass
import java.time.Clock
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class JsonMappableBigMoverReport
internal constructor(
    override val id: BigMoverReport.Id,
    override val symbol: StockSymbol,
    override val lastState: MarketState,
    override val lastNotified: LocalDateTime,
    override val lastPercent: StockPercent,
    override val lastPrice: StockMoneyValue,
) : BigMoverReport {

  override fun lastNotified(notified: LocalDateTime): BigMoverReport {
    return this.copy(lastNotified = notified)
  }

  override fun lastState(state: MarketState): BigMoverReport {
    return this.copy(lastState = state)
  }

  override fun lastPercent(percent: StockPercent): BigMoverReport {
    return this.copy(lastPercent = percent)
  }

  override fun lastPrice(price: StockMoneyValue): BigMoverReport {
    return this.copy(lastPrice = price)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(quote: StockQuote, clock: Clock): BigMoverReport {
      val session = quote.currentSession

      return JsonMappableBigMoverReport(
          id = BigMoverReport.Id(IdGenerator.generate()),
          symbol = quote.symbol,
          lastPrice = session.price,
          lastPercent = session.percent,
          lastState = session.state,
          lastNotified = LocalDateTime.now(clock),
      )
    }

    @JvmStatic
    @CheckResult
    fun from(item: BigMoverReport): JsonMappableBigMoverReport {
      return if (item is JsonMappableBigMoverReport) item
      else {
        JsonMappableBigMoverReport(
            item.id,
            item.symbol,
            item.lastState,
            item.lastNotified,
            item.lastPercent,
            item.lastPrice,
        )
      }
    }
  }
}
