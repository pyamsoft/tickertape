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

package com.pyamsoft.tickertape.db.room.dao.position

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionInsertDao
import com.pyamsoft.tickertape.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.tickertape.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.tickertape.db.room.entity.RoomDbPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomPositionInsertDao : PositionInsertDao {

  override suspend fun insert(o: DbPosition): DbInsert.InsertResult<DbPosition> =
      withContext(context = Dispatchers.IO) {
        val roomPosition = RoomDbPosition.create(o)
        return@withContext if (daoQuery(roomPosition.id()) == null) {
          if (daoInsert(roomPosition) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomPosition)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomPosition,
                error = IllegalStateException("Unable to insert position $roomPosition"),
            )
          }
        } else {
          if (daoUpdate(roomPosition) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomPosition)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomPosition,
                error = IllegalStateException("Unable to insert position $roomPosition"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbPosition): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbPosition.TABLE_NAME} WHERE
        ${RoomDbPosition.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbPosition.Id): RoomDbPosition?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbPosition): Int
}
