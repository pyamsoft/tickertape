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

package com.pyamsoft.tickertape.db.room.dao.symbol

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.tickertape.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.tickertape.db.room.entity.RoomDbSymbol
import com.pyamsoft.tickertape.db.symbol.DbSymbol
import com.pyamsoft.tickertape.db.symbol.SymbolInsertDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomSymbolInsertDao : SymbolInsertDao {

  override suspend fun insert(o: DbSymbol): DbInsert.InsertResult<DbSymbol> =
      withContext(context = Dispatchers.IO) {
        val roomSymbol = RoomDbSymbol.create(o)
        return@withContext if (daoQuery(roomSymbol.id()) == null) {
          if (daoInsert(roomSymbol) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomSymbol)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomSymbol,
                error = IllegalStateException("Unable to insert symbol $roomSymbol"),
            )
          }
        } else {
          if (daoUpdate(roomSymbol) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomSymbol)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomSymbol,
                error = IllegalStateException("Unable to insert symbol $roomSymbol"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbSymbol): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbSymbol.TABLE_NAME} WHERE
        ${RoomDbSymbol.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbSymbol.Id): RoomDbSymbol?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbSymbol): Int
}
