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

package com.pyamsoft.tickertape.db.room.holding.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

@Entity(tableName = RoomDbHolding.TABLE_NAME)
internal data class RoomDbHolding
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbHolding.Id,
    @JvmField @ColumnInfo(name = COLUMN_SYMBOL) val dbSymbol: StockSymbol,
    @JvmField @ColumnInfo(name = COLUMN_HOLDING_TYPE) val dbType: EquityType,
    @JvmField @ColumnInfo(name = COLUMN_HOLDING_SIDE) val dbSide: TradeSide,
) : DbHolding {

  @Ignore override val id: DbHolding.Id = dbId

  @Ignore override val symbol: StockSymbol = dbSymbol

  @Ignore override val type: EquityType = dbType

  @Ignore override val side: TradeSide = dbSide

  companion object {

    @Ignore internal const val TABLE_NAME = "room_holding_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_SYMBOL = "symbol"

    @Ignore internal const val COLUMN_HOLDING_TYPE = "holding_type"

    @Ignore internal const val COLUMN_HOLDING_SIDE = "holding_side"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbHolding): RoomDbHolding {
      return if (item is RoomDbHolding) item
      else {
        RoomDbHolding(
            item.id,
            item.symbol,
            item.type,
            item.side,
        )
      }
    }
  }
}
