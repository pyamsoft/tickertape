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

package com.pyamsoft.tickertape.db.position

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class JsonMappableDbPosition
internal constructor(
    internal val id: DbPosition.Id,
    internal val holdingId: DbHolding.Id,
    internal val shareCount: StockShareValue,
    internal val price: StockMoneyValue,
    internal val purchaseDate: LocalDateTime,
) : DbPosition {

  override fun id(): DbPosition.Id {
    return id
  }

  override fun holdingId(): DbHolding.Id {
    return holdingId
  }

  override fun shareCount(): StockShareValue {
    return shareCount
  }

  override fun shareCount(shareCount: StockShareValue): DbPosition {
    return this.copy(shareCount = shareCount)
  }

  override fun price(): StockMoneyValue {
    return price
  }

  override fun price(price: StockMoneyValue): DbPosition {
    return this.copy(price = price)
  }

  override fun purchaseDate(): LocalDateTime {
    return purchaseDate
  }

  override fun purchaseDate(purchaseDate: LocalDateTime): DbPosition {
    return this.copy(purchaseDate = purchaseDate)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        holdingId: DbHolding.Id,
        shareCount: StockShareValue,
        price: StockMoneyValue,
        purchaseDate: LocalDateTime,
        id: DbPosition.Id = DbPosition.Id(IdGenerator.generate()),
    ): DbPosition {
      return JsonMappableDbPosition(
          id = id,
          holdingId = holdingId,
          shareCount = shareCount,
          price = price,
          purchaseDate = purchaseDate,
      )
    }

    @JvmStatic
    @CheckResult
    fun from(item: DbPosition): JsonMappableDbPosition {
      return if (item is JsonMappableDbPosition) item
      else {
        JsonMappableDbPosition(
            item.id(),
            item.holdingId(),
            item.shareCount(),
            item.price(),
            item.purchaseDate(),
        )
      }
    }
  }
}
