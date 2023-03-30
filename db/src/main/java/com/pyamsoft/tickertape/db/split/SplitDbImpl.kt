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

package com.pyamsoft.tickertape.db.split

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SplitDbImpl
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @DbApi realQueryDao: SplitQueryDao,
    @DbApi private val realInsertDao: SplitInsertDao,
    @DbApi private val realDeleteDao: SplitDeleteDao,
) :
    SplitDb,
    SplitQueryDao.Cache,
    BaseDbImpl<
        SplitChangeEvent,
        SplitRealtime,
        SplitQueryDao,
        SplitInsertDao,
        SplitDeleteDao,
    >() {

  private val queryCache =
      cachify<List<DbSplit>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbSplit>, DbSplit.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  private val queryByHoldingIdCache =
      multiCachify<QueryByHoldingIdKey, List<DbSplit>, DbHolding.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryByHoldingId(id)
      }

  override val deleteDao: SplitDeleteDao = this

  override val insertDao: SplitInsertDao = this

  override val queryDao: SplitQueryDao = this

  override val realtime: SplitRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        queryCache.clear()
        queryByIdCache.clear()
        queryByHoldingIdCache.clear()
      }

  override suspend fun invalidateById(id: DbSplit.Id) =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                id = id,
            )
        return@withContext queryByIdCache.key(key).clear()
      }

  override suspend fun invalidateByHoldingId(id: DbHolding.Id) =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByHoldingIdKey(
                id = id,
            )
        return@withContext queryByHoldingIdCache.key(key).clear()
      }

  override suspend fun listenForChanges(onChange: (event: SplitChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) { onEvent(onChange) }

  override suspend fun query(): List<DbSplit> =
      withContext(context = Dispatchers.IO) { queryCache.call() }

  override suspend fun queryById(id: DbSplit.Id): Maybe<out DbSplit> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                id = id,
            )
        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun queryByHoldingId(id: DbHolding.Id): List<DbSplit> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByHoldingIdKey(
                id = id,
            )
        return@withContext queryByHoldingIdCache.key(key).call(id)
      }

  override suspend fun insert(o: DbSplit): DbInsert.InsertResult<DbSplit> =
      withContext(context = Dispatchers.IO) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(SplitChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(SplitChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbSplit, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            invalidate()
            publish(SplitChangeEvent.Delete(o, offerUndo))
          }
        }
      }

  private data class QueryByIdKey(
      val id: DbSplit.Id,
  )

  private data class QueryByHoldingIdKey(
      val id: DbHolding.Id,
  )
}
