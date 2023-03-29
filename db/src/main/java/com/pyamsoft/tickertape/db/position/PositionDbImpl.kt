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

package com.pyamsoft.tickertape.db.position

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PositionDbImpl
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @DbApi realQueryDao: PositionQueryDao,
    @DbApi private val realInsertDao: PositionInsertDao,
    @DbApi private val realDeleteDao: PositionDeleteDao,
) :
    PositionDb,
    PositionQueryDao.Cache,
    BaseDbImpl<
        PositionChangeEvent,
        PositionRealtime,
        PositionQueryDao,
        PositionInsertDao,
        PositionDeleteDao,
    >() {

  private val queryCache =
      cachify<List<DbPosition>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbPosition>, DbPosition.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  private val queryByHoldingIdCache =
      multiCachify<QueryByHoldingIdKey, List<DbPosition>, DbHolding.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryByHoldingId(id)
      }

  override val deleteDao: PositionDeleteDao = this

  override val insertDao: PositionInsertDao = this

  override val queryDao: PositionQueryDao = this

  override val realtime: PositionRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        queryCache.clear()
        queryByIdCache.clear()
        queryByHoldingIdCache.clear()
      }

  override suspend fun invalidateById(id: DbPosition.Id) =
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

  override suspend fun listenForChanges(onChange: (event: PositionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) { onEvent(onChange) }

  override suspend fun query(): List<DbPosition> =
      withContext(context = Dispatchers.IO) { queryCache.call() }

  override suspend fun queryById(id: DbPosition.Id): Maybe<out DbPosition> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                id = id,
            )
        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun queryByHoldingId(id: DbHolding.Id): List<DbPosition> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByHoldingIdKey(
                id = id,
            )
        return@withContext queryByHoldingIdCache.key(key).call(id)
      }

  override suspend fun insert(o: DbPosition): DbInsert.InsertResult<DbPosition> =
      withContext(context = Dispatchers.IO) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(PositionChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(PositionChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbPosition, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            invalidate()
            publish(PositionChangeEvent.Delete(o, offerUndo))
          }
        }
      }

  private data class QueryByIdKey(
      val id: DbPosition.Id,
  )

  private data class QueryByHoldingIdKey(
      val id: DbHolding.Id,
  )
}
