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

package com.pyamsoft.tickertape.db.room.dao.symbol

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import com.pyamsoft.tickertape.db.room.entity.RoomDbSymbol
import com.pyamsoft.tickertape.db.symbol.DbSymbol
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomSymbolQueryDao : SymbolQueryDao {

  override suspend fun query(): List<DbSymbol> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Query("""SELECT * FROM ${RoomDbSymbol.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbSymbol>
}
