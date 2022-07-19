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

package com.pyamsoft.tickertape.db.room.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.LocalDateTime

@Entity(
    tableName = RoomDbSplit.TABLE_NAME,
    foreignKeys =
        [
            ForeignKey(
                entity = RoomDbHolding::class,
                parentColumns = [RoomDbHolding.COLUMN_ID],
                childColumns = [RoomDbSplit.COLUMN_HOLDING_ID],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
)
internal data class RoomDbSplit
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbSplit.Id,
    @JvmField @ColumnInfo(name = COLUMN_HOLDING_ID, index = true) val dbHoldingId: DbHolding.Id,
    @JvmField
    @ColumnInfo(name = COLUMN_PRE_SPLIT_SHARE_COUNT)
    val dbPreSplitShareCount: StockShareValue,
    @JvmField
    @ColumnInfo(name = COLUMN_POST_SPLIT_SHARE_COUNT)
    val dbPostSplitShareCount: StockShareValue,
    @JvmField @ColumnInfo(name = COLUMN_SPLIT_DATE) val dbSplitDate: LocalDateTime,
) : DbSplit {

  @Ignore override val id: DbSplit.Id = dbId

  @Ignore override val holdingId: DbHolding.Id = dbHoldingId

  @Ignore override val preSplitShareCount: StockShareValue = dbPreSplitShareCount

  @Ignore override val postSplitShareCount: StockShareValue = dbPostSplitShareCount

  @Ignore override val splitDate: LocalDateTime = dbSplitDate

  @Ignore
  override fun preSplitShareCount(shareCount: StockShareValue): DbSplit {
    return this.copy(dbPreSplitShareCount = shareCount)
  }

  @Ignore
  override fun postSplitShareCount(shareCount: StockShareValue): DbSplit {
    return this.copy(dbPostSplitShareCount = shareCount)
  }

  @Ignore
  override fun splitDate(date: LocalDateTime): DbSplit {
    return this.copy(dbSplitDate = date)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_split_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_HOLDING_ID = "holding_id"

    @Ignore internal const val COLUMN_PRE_SPLIT_SHARE_COUNT = "pre_split_share_count"

    @Ignore internal const val COLUMN_POST_SPLIT_SHARE_COUNT = "post_split_share_count"

    @Ignore internal const val COLUMN_SPLIT_DATE = "split_date"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbSplit): RoomDbSplit {
      return if (item is RoomDbSplit) item
      else {
        RoomDbSplit(
            item.id,
            item.holdingId,
            item.preSplitShareCount,
            item.postSplitShareCount,
            item.splitDate,
        )
      }
    }
  }
}
