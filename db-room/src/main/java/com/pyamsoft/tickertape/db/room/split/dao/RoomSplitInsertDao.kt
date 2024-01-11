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

package com.pyamsoft.tickertape.db.room.split.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.tickertape.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.tickertape.db.room.split.entity.RoomDbSplit
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitInsertDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomSplitInsertDao : SplitInsertDao {

  @Transaction
  override suspend fun insert(o: DbSplit): DbInsert.InsertResult<DbSplit> =
      withContext(context = Dispatchers.Default) {
        val roomSplit = RoomDbSplit.create(o)
        return@withContext if (daoQuery(roomSplit.id) == null) {
          if (daoInsert(roomSplit) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomSplit)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomSplit,
                error = IllegalStateException("Unable to insert position $roomSplit"),
            )
          }
        } else {
          if (daoUpdate(roomSplit) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomSplit)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomSplit,
                error = IllegalStateException("Unable to insert position $roomSplit"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbSplit): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbSplit.TABLE_NAME} WHERE
        ${RoomDbSplit.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbSplit.Id): RoomDbSplit?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbSplit): Int
}
