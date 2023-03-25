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
import com.pyamsoft.tickertape.db.symbol.DbSymbol
import com.pyamsoft.tickertape.stocks.api.StockSymbol

@Deprecated("Don't use")
@Entity(tableName = RoomDbSymbol.TABLE_NAME)
internal data class RoomDbSymbol
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbSymbol.Id,
    @JvmField @ColumnInfo(name = COLUMN_SYMBOL) val dbSymbol: StockSymbol
) : DbSymbol {

  @Ignore override val id: DbSymbol.Id = dbId

  @Ignore override val symbol: StockSymbol = dbSymbol

  companion object {

    @Ignore internal const val TABLE_NAME = "room_symbol_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_SYMBOL = "symbol"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(symbol: DbSymbol): RoomDbSymbol {
      return if (symbol is RoomDbSymbol) symbol
      else {
        RoomDbSymbol(
            symbol.id,
            symbol.symbol,
        )
      }
    }
  }
}
