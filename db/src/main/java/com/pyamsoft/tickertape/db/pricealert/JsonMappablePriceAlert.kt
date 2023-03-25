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

package com.pyamsoft.tickertape.db.pricealert

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class JsonMappablePriceAlert
internal constructor(
    override val id: PriceAlert.Id,
    override val symbol: StockSymbol,
    override val lastNotified: LocalDateTime?,
    override val triggerPriceAbove: StockMoneyValue?,
    override val triggerPriceBelow: StockMoneyValue?,
    override val enabled: Boolean,
) : PriceAlert {

  override fun lastNotified(notified: LocalDateTime): PriceAlert {
    return this.copy(lastNotified = notified)
  }

  override fun watchForPriceAbove(price: StockMoneyValue): PriceAlert {
    return this.copy(triggerPriceAbove = price)
  }

  override fun clearPriceAbove(): PriceAlert {
    return this.copy(triggerPriceAbove = null)
  }

  override fun watchForPriceBelow(price: StockMoneyValue): PriceAlert {
    return this.copy(triggerPriceBelow = price)
  }

  override fun clearPriceBelow(): PriceAlert {
    return this.copy(triggerPriceBelow = null)
  }

  override fun enable(): PriceAlert {
    return this.copy(enabled = true)
  }

  override fun disable(): PriceAlert {
    return this.copy(enabled = false)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        priceAbove: StockMoneyValue?,
        priceBelow: StockMoneyValue?,
    ): PriceAlert {
      return JsonMappablePriceAlert(
          id = PriceAlert.Id(IdGenerator.generate()),
          symbol = symbol,
          lastNotified = null,
          triggerPriceAbove = priceAbove,
          triggerPriceBelow = priceBelow,
          enabled = true,
      )
    }

    @JvmStatic
    @CheckResult
    fun from(item: PriceAlert): JsonMappablePriceAlert {
      return if (item is JsonMappablePriceAlert) item
      else {
        JsonMappablePriceAlert(
            item.id,
            item.symbol,
            item.lastNotified,
            item.triggerPriceAbove,
            item.triggerPriceBelow,
            item.enabled,
        )
      }
    }
  }
}
