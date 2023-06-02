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

package com.pyamsoft.tickertape.db.room.position.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.room.position.entity.RoomDbPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomPositionQueryDao : PositionQueryDao {

  final override suspend fun query(): List<DbPosition> =
      withContext(context = Dispatchers.Default) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""SELECT * FROM ${RoomDbPosition.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbPosition>

  final override suspend fun queryById(id: DbPosition.Id): Maybe<out DbPosition> =
      withContext(context = Dispatchers.Default) {
        when (val res = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(res)
        }
      }

  @CheckResult
  @Query(
      """
      SELECT * FROM ${RoomDbPosition.TABLE_NAME}
      WHERE ${RoomDbPosition.COLUMN_ID} = :id
      LIMIT 1
      """)
  internal abstract suspend fun daoQueryById(id: DbPosition.Id): RoomDbPosition?

  final override suspend fun queryByHoldingId(id: DbHolding.Id): List<DbPosition> =
      withContext(context = Dispatchers.Default) { daoQueryByHoldingId(id) }

  @CheckResult
  @Query(
      """
      SELECT * FROM ${RoomDbPosition.TABLE_NAME}
      WHERE ${RoomDbPosition.COLUMN_HOLDING_ID} = :id
      """)
  internal abstract suspend fun daoQueryByHoldingId(id: DbHolding.Id): List<RoomDbPosition>
}
