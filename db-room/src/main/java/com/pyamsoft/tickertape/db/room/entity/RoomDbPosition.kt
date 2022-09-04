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
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import java.time.LocalDate

@Entity(
    tableName = RoomDbPosition.TABLE_NAME,
    foreignKeys =
        [
            ForeignKey(
                entity = RoomDbHolding::class,
                parentColumns = [RoomDbHolding.COLUMN_ID],
                childColumns = [RoomDbPosition.COLUMN_HOLDING_ID],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
)
internal data class RoomDbPosition
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbPosition.Id,
    @JvmField @ColumnInfo(name = COLUMN_HOLDING_ID, index = true) val dbHoldingId: DbHolding.Id,
    @JvmField @ColumnInfo(name = COLUMN_PRICE) val dbPrice: StockMoneyValue,
    @JvmField @ColumnInfo(name = COLUMN_SHARE_COUNT) val dbShareCount: StockShareValue,
    @JvmField @ColumnInfo(name = COLUMN_PURCHASE_DATE) val dbPurchaseDate: LocalDate,
) : DbPosition {

  @Ignore override val id: DbPosition.Id = dbId

  @Ignore override val holdingId: DbHolding.Id = dbHoldingId

  @Ignore override val price: StockMoneyValue = dbPrice

  @Ignore override val shareCount: StockShareValue = dbShareCount

  @Ignore override val purchaseDate: LocalDate = dbPurchaseDate

  @Ignore
  override fun price(price: StockMoneyValue): DbPosition {
    return this.copy(dbPrice = price)
  }

  @Ignore
  override fun shareCount(shareCount: StockShareValue): DbPosition {
    return this.copy(dbShareCount = shareCount)
  }

  @Ignore
  override fun purchaseDate(purchaseDate: LocalDate): DbPosition {
    return this.copy(dbPurchaseDate = purchaseDate)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_position_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_HOLDING_ID = "holding_id"

    @Ignore internal const val COLUMN_PRICE = "price"

    @Ignore internal const val COLUMN_SHARE_COUNT = "share_count"

    @Ignore internal const val COLUMN_PURCHASE_DATE = "purchase_date"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbPosition): RoomDbPosition {
      return if (item is RoomDbPosition) item
      else {
        RoomDbPosition(
            item.id,
            item.holdingId,
            item.price,
            item.shareCount,
            item.purchaseDate,
        )
      }
    }
  }
}
