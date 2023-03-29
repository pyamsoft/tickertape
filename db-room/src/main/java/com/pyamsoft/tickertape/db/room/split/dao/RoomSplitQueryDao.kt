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

package com.pyamsoft.tickertape.db.room.split.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.room.split.entity.RoomDbSplit
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomSplitQueryDao : SplitQueryDao {

  final override suspend fun query(): List<DbSplit> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""SELECT * FROM ${RoomDbSplit.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbSplit>

  final override suspend fun queryById(id: DbSplit.Id): Maybe<out DbSplit> =
      withContext(context = Dispatchers.IO) {
        when (val res = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(res)
        }
      }

  @CheckResult
  @Query(
      """
      SELECT * FROM ${RoomDbSplit.TABLE_NAME}
      WHERE ${RoomDbSplit.COLUMN_ID} = :id
      LIMIT 1
      """)
  internal abstract suspend fun daoQueryById(id: DbSplit.Id): RoomDbSplit?

  final override suspend fun queryByHoldingId(id: DbHolding.Id): List<DbSplit> =
      withContext(context = Dispatchers.IO) { daoQueryByHoldingId(id) }

  @CheckResult
  @Query(
      """
      SELECT * FROM ${RoomDbSplit.TABLE_NAME}
      WHERE ${RoomDbSplit.COLUMN_HOLDING_ID} = :id
      """)
  internal abstract suspend fun daoQueryByHoldingId(id: DbHolding.Id): List<RoomDbSplit>
}
