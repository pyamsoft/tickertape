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

package com.pyamsoft.tickertape.db.room.pricealert.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.db.pricealert.PriceAlertQueryDao
import com.pyamsoft.tickertape.db.room.pricealert.entity.RoomPriceAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomPriceAlertQueryDao : PriceAlertQueryDao {

  final override suspend fun query(): List<PriceAlert> =
      withContext(context = Dispatchers.Default) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""SELECT * FROM ${RoomPriceAlert.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomPriceAlert>

  final override suspend fun queryActive(): List<PriceAlert> =
      withContext(context = Dispatchers.Default) { daoQueryActive() }

  @CheckResult
  @Transaction
  @Query(
      """
      SELECT * FROM ${RoomPriceAlert.TABLE_NAME}
      WHERE ${RoomPriceAlert.COLUMN_ENABLED} = TRUE
      AND
        (
          ${RoomPriceAlert.COLUMN_TRIGGER_PRICE_BELOW} IS NOT NULL
          OR ${RoomPriceAlert.COLUMN_TRIGGER_PRICE_ABOVE} IS NOT NULL
        )
      """)
  internal abstract suspend fun daoQueryActive(): List<RoomPriceAlert>
}
