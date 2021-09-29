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

package com.pyamsoft.tickertape.db.room.dao.mover

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import com.pyamsoft.tickertape.db.mover.BigMoverQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverReport
import com.pyamsoft.tickertape.db.room.entity.RoomBigMoverReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomBigMoverQueryDao : BigMoverQueryDao {

  override suspend fun query(force: Boolean): List<BigMoverReport> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Query("""SELECT * FROM ${RoomBigMoverReport.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomBigMoverReport>
}
