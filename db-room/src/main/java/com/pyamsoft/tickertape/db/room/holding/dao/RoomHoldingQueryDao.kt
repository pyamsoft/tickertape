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

package com.pyamsoft.tickertape.db.room.holding.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.room.holding.entity.RoomDbHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomHoldingQueryDao : HoldingQueryDao {

  final override suspend fun query(): List<DbHolding> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""SELECT * FROM ${RoomDbHolding.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbHolding>
}