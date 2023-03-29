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

package com.pyamsoft.tickertape.db.holding

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class HoldingDbImpl
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @DbApi realQueryDao: HoldingQueryDao,
    @DbApi private val realInsertDao: HoldingInsertDao,
    @DbApi private val realDeleteDao: HoldingDeleteDao,
) :
    HoldingDb,
    HoldingQueryDao.Cache,
    BaseDbImpl<
        HoldingChangeEvent,
        HoldingRealtime,
        HoldingQueryDao,
        HoldingInsertDao,
        HoldingDeleteDao,
    >() {

  private val queryCache =
      cachify<List<DbHolding>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbHolding>, DbHolding.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  override val deleteDao: HoldingDeleteDao = this

  override val insertDao: HoldingInsertDao = this

  override val queryDao: HoldingQueryDao = this

  override val realtime: HoldingRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        queryCache.clear()
        queryByIdCache.clear()
      }

  override suspend fun invalidateById(id: DbHolding.Id) =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                id = id,
            )
        queryByIdCache.key(key).clear()
      }

  override suspend fun listenForChanges(onChange: (event: HoldingChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) { onEvent(onChange) }

  override suspend fun query(): List<DbHolding> =
      withContext(context = Dispatchers.IO) { queryCache.call() }

  override suspend fun queryById(id: DbHolding.Id): Maybe<out DbHolding> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                id = id,
            )
        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun insert(o: DbHolding): DbInsert.InsertResult<DbHolding> =
      withContext(context = Dispatchers.IO) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(HoldingChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(HoldingChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbHolding, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            invalidate()
            publish(HoldingChangeEvent.Delete(o, offerUndo))
          }
        }
      }

  private data class QueryByIdKey(
      val id: DbHolding.Id,
  )
}
