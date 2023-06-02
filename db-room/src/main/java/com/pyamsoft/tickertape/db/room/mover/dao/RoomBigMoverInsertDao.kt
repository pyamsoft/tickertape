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

package com.pyamsoft.tickertape.db.room.mover.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.mover.BigMoverInsertDao
import com.pyamsoft.tickertape.db.mover.BigMoverReport
import com.pyamsoft.tickertape.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.tickertape.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.tickertape.db.room.mover.entity.RoomBigMoverReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomBigMoverInsertDao : BigMoverInsertDao {

  @Transaction
  override suspend fun insert(o: BigMoverReport): DbInsert.InsertResult<BigMoverReport> =
      withContext(context = Dispatchers.Default) {
        val roomBigMover = RoomBigMoverReport.create(o)
        return@withContext if (daoQuery(roomBigMover.id) == null) {
          if (daoInsert(roomBigMover) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomBigMover)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomBigMover,
                error = IllegalStateException("Unable to update bigmover $roomBigMover"),
            )
          }
        } else {
          if (daoUpdate(roomBigMover) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomBigMover)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomBigMover,
                error = IllegalStateException("Unable to update bigmover $roomBigMover"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomBigMoverReport): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomBigMoverReport.TABLE_NAME} WHERE
        ${RoomBigMoverReport.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: BigMoverReport.Id): RoomBigMoverReport?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomBigMoverReport): Int
}
