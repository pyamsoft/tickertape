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

package com.pyamsoft.tickertape.db.room.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.tickertape.db.mover.BigMoverReport
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

@Entity(tableName = RoomBigMoverReport.TABLE_NAME)
internal data class RoomBigMoverReport
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: BigMoverReport.Id,
    @JvmField @ColumnInfo(name = COLUMN_SYMBOL) val dbSymbol: StockSymbol,
    @JvmField @ColumnInfo(name = COLUMN_LAST_DATE) val dbLastNotified: LocalDateTime,
    @JvmField @ColumnInfo(name = COLUMN_LAST_PERCENT) val dbLastPercent: StockPercent,
    @JvmField @ColumnInfo(name = COLUMN_LAST_PRICE) val dbLastPrice: StockMoneyValue,
    @JvmField @ColumnInfo(name = COLUMN_LAST_STATE) val dbLastState: MarketState,
) : BigMoverReport {

  @Ignore override val id: BigMoverReport.Id = dbId

  @Ignore override val symbol: StockSymbol = dbSymbol

  @Ignore override val lastNotified: LocalDateTime = dbLastNotified

  @Ignore override val lastPercent: StockPercent = dbLastPercent

  @Ignore override val lastPrice: StockMoneyValue = dbLastPrice

  @Ignore override val lastState: MarketState = dbLastState

  @Ignore
  override fun lastNotified(notified: LocalDateTime): BigMoverReport {
    return this.copy(dbLastNotified = notified)
  }

  @Ignore
  override fun lastPercent(percent: StockPercent): BigMoverReport {
    return this.copy(dbLastPercent = percent)
  }

  @Ignore
  override fun lastPrice(price: StockMoneyValue): BigMoverReport {
    return this.copy(dbLastPrice = price)
  }

  @Ignore
  override fun lastState(state: MarketState): BigMoverReport {
    return this.copy(dbLastState = state)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_big_mover_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_SYMBOL = "symbol"

    @Ignore internal const val COLUMN_LAST_DATE = "last_date"

    @Ignore internal const val COLUMN_LAST_PERCENT = "last_percent"

    @Ignore internal const val COLUMN_LAST_PRICE = "last_price"

    @Ignore internal const val COLUMN_LAST_STATE = "last_state"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: BigMoverReport): RoomBigMoverReport {
      return if (item is RoomBigMoverReport) item
      else {
        RoomBigMoverReport(
            item.id,
            item.symbol,
            item.lastNotified,
            item.lastPercent,
            item.lastPrice,
            item.lastState,
        )
      }
    }
  }
}
