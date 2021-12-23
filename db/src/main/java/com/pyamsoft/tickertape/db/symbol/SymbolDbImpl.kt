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

package com.pyamsoft.tickertape.db.symbol

import com.pyamsoft.cachify.cachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.DbInsert
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SymbolDbImpl
@Inject
internal constructor(
    @DbApi realQueryDao: SymbolQueryDao,
    @param:DbApi private val realInsertDao: SymbolInsertDao,
    @param:DbApi private val realDeleteDao: SymbolDeleteDao,
) :
    BaseDbImpl<
        SymbolChangeEvent, SymbolRealtime, SymbolQueryDao, SymbolInsertDao, SymbolDeleteDao>(),
    SymbolDb {

  private val queryCache =
      cachify<List<DbSymbol>> {
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query(true)
      }

  override fun realtime(): SymbolRealtime {
    return this
  }

  override fun queryDao(): SymbolQueryDao {
    return this
  }

  override fun insertDao(): SymbolInsertDao {
    return this
  }

  override fun deleteDao(): SymbolDeleteDao {
    return this
  }

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
      }

  override suspend fun listenForChanges(onChange: suspend (event: SymbolChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(force: Boolean): List<DbSymbol> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        if (force) {
          invalidate()
        }

        return@withContext queryCache.call()
      }

  override suspend fun insert(o: DbSymbol): DbInsert.InsertResult<DbSymbol> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> publish(SymbolChangeEvent.Insert(result.data))
            is DbInsert.InsertResult.Update -> publish(SymbolChangeEvent.Update(result.data))
            is DbInsert.InsertResult.Fail -> Timber.e(result.error, "Insert attempt failed: $o")
          }
        }
      }

  override suspend fun delete(o: DbSymbol, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            publish(SymbolChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}
