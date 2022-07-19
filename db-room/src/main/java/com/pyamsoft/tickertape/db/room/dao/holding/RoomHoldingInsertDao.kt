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

package com.pyamsoft.tickertape.db.room.dao.holding

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.tickertape.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.tickertape.db.room.entity.RoomDbHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomHoldingInsertDao : HoldingInsertDao {

  override suspend fun insert(o: DbHolding): DbInsert.InsertResult<DbHolding> =
      withContext(context = Dispatchers.IO) {
        val roomHolding = RoomDbHolding.create(o)
        return@withContext if (daoQuery(roomHolding.id) == null) {
          if (daoInsert(roomHolding) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomHolding)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomHolding,
                error = IllegalStateException("Unable to update holding $roomHolding"),
            )
          }
        } else {
          if (daoUpdate(roomHolding) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomHolding)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomHolding,
                error = IllegalStateException("Unable to update holding $roomHolding"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbHolding): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbHolding.TABLE_NAME} WHERE
        ${RoomDbHolding.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbHolding.Id): RoomDbHolding?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbHolding): Int
}
