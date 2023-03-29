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
import androidx.room.Delete
import com.pyamsoft.tickertape.db.mover.BigMoverDeleteDao
import com.pyamsoft.tickertape.db.mover.BigMoverReport
import com.pyamsoft.tickertape.db.room.mover.entity.RoomBigMoverReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomBigMoverDeleteDao : BigMoverDeleteDao {

  override suspend fun delete(o: BigMoverReport, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        val roomBigMover = RoomBigMoverReport.create(o)
        return@withContext daoDelete(roomBigMover) > 0
      }

  @Delete @CheckResult internal abstract fun daoDelete(symbol: RoomBigMoverReport): Int
}
