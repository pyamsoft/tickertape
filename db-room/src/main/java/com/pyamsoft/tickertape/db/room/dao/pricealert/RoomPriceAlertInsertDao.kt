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

package com.pyamsoft.tickertape.db.room.dao.pricealert

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.db.pricealert.PriceAlertInsertDao
import com.pyamsoft.tickertape.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.tickertape.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.tickertape.db.room.entity.RoomPriceAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomPriceAlertInsertDao : PriceAlertInsertDao {

  override suspend fun insert(o: PriceAlert): DbInsert.InsertResult<PriceAlert> =
      withContext(context = Dispatchers.IO) {
        val roomPriceAlert = RoomPriceAlert.create(o)
        return@withContext if (daoQuery(roomPriceAlert.id) == null) {
          if (daoInsert(roomPriceAlert) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomPriceAlert)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomPriceAlert,
                error = IllegalStateException("Unable to update pricealert $roomPriceAlert"),
            )
          }
        } else {
          if (daoUpdate(roomPriceAlert) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomPriceAlert)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomPriceAlert,
                error = IllegalStateException("Unable to update pricealert $roomPriceAlert"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomPriceAlert): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomPriceAlert.TABLE_NAME} WHERE
        ${RoomPriceAlert.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: PriceAlert.Id): RoomPriceAlert?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomPriceAlert): Int
}
