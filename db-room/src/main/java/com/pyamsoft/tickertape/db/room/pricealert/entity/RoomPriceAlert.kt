/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.db.room.pricealert.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

@Entity(tableName = RoomPriceAlert.TABLE_NAME)
internal data class RoomPriceAlert
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: PriceAlert.Id,
    @JvmField @ColumnInfo(name = COLUMN_SYMBOL) val dbSymbol: StockSymbol,
    @JvmField @ColumnInfo(name = COLUMN_LAST_DATE) val dbLastNotified: LocalDateTime?,
    @JvmField @ColumnInfo(name = COLUMN_TRIGGER_PRICE_ABOVE) val dbPriceAbove: StockMoneyValue?,
    @JvmField @ColumnInfo(name = COLUMN_TRIGGER_PRICE_BELOW) val dbPriceBelow: StockMoneyValue?,
    @JvmField @ColumnInfo(name = COLUMN_ENABLED) val dbEnabled: Boolean,
) : PriceAlert {

  @Ignore override val id: PriceAlert.Id = dbId

  @Ignore override val symbol: StockSymbol = dbSymbol

  @Ignore override val lastNotified: LocalDateTime? = dbLastNotified

  @Ignore override val triggerPriceAbove: StockMoneyValue? = dbPriceAbove

  @Ignore override val triggerPriceBelow: StockMoneyValue? = dbPriceBelow

  @Ignore override val enabled: Boolean = dbEnabled

  @Ignore
  override fun lastNotified(notified: LocalDateTime): PriceAlert {
    return this.copy(dbLastNotified = notified)
  }

  @Ignore
  override fun watchForPriceAbove(price: StockMoneyValue): PriceAlert {
    return this.copy(dbPriceAbove = price)
  }

  @Ignore
  override fun clearPriceAbove(): PriceAlert {
    return this.copy(dbPriceAbove = null)
  }

  @Ignore
  override fun watchForPriceBelow(price: StockMoneyValue): PriceAlert {
    return this.copy(dbPriceBelow = price)
  }

  @Ignore
  override fun clearPriceBelow(): PriceAlert {
    return this.copy(dbPriceBelow = null)
  }

  @Ignore
  override fun enable(): PriceAlert {
    return this.copy(dbEnabled = true)
  }

  @Ignore
  override fun disable(): PriceAlert {
    return this.copy(dbEnabled = false)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_price_alert_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_SYMBOL = "symbol"

    @Ignore internal const val COLUMN_LAST_DATE = "last_date"

    @Ignore internal const val COLUMN_TRIGGER_PRICE_ABOVE = "trigger_price_above"

    @Ignore internal const val COLUMN_TRIGGER_PRICE_BELOW = "trigger_price_below"

    @Ignore internal const val COLUMN_ENABLED = "enabled"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: PriceAlert): RoomPriceAlert {
      return if (item is RoomPriceAlert) item
      else {
        RoomPriceAlert(
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
