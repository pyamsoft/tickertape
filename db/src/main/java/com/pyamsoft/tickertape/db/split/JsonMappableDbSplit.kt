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

package com.pyamsoft.tickertape.db.split

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class JsonMappableDbSplit
internal constructor(
    internal val id: DbSplit.Id,
    internal val holdingId: DbHolding.Id,
    internal val preSplitShareCount: StockShareValue,
    internal val postSplitShareCount: StockShareValue,
    internal val splitDate: LocalDateTime,
) : DbSplit {

  override fun id(): DbSplit.Id {
    return id
  }

  override fun holdingId(): DbHolding.Id {
    return holdingId
  }

  override fun preSplitShareCount(): StockShareValue {
    return preSplitShareCount
  }

  override fun preSplitShareCount(shareCount: StockShareValue): DbSplit {
    return this.copy(preSplitShareCount = shareCount)
  }

  override fun postSplitShareCount(): StockShareValue {
    return postSplitShareCount
  }

  override fun postSplitShareCount(shareCount: StockShareValue): DbSplit {
    return this.copy(postSplitShareCount = shareCount)
  }

  override fun splitDate(): LocalDateTime {
    return splitDate
  }

  override fun splitDate(date: LocalDateTime): DbSplit {
    return this.copy(splitDate = splitDate)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        holdingId: DbHolding.Id,
        preSplitShareCount: StockShareValue,
        postSplitShareCount: StockShareValue,
        splitDate: LocalDateTime,
        id: DbSplit.Id = DbSplit.Id(IdGenerator.generate()),
    ): DbSplit {
      return JsonMappableDbSplit(
          id = id,
          holdingId = holdingId,
          preSplitShareCount = preSplitShareCount,
          postSplitShareCount = postSplitShareCount,
          splitDate = splitDate,
      )
    }

    @JvmStatic
    @CheckResult
    fun from(item: DbSplit): JsonMappableDbSplit {
      return if (item is JsonMappableDbSplit) item
      else {
        JsonMappableDbSplit(
            item.id(),
            item.holdingId(),
            item.preSplitShareCount(),
            item.postSplitShareCount(),
            item.splitDate(),
        )
      }
    }
  }
}
