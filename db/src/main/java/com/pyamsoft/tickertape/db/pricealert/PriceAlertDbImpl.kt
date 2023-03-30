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

package com.pyamsoft.tickertape.db.pricealert

import com.pyamsoft.cachify.cachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import com.pyamsoft.tickertape.db.DbInsert
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class PriceAlertDbImpl
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @DbApi realQueryDao: PriceAlertQueryDao,
    @DbApi private val realInsertDao: PriceAlertInsertDao,
    @DbApi private val realDeleteDao: PriceAlertDeleteDao,
) :
    PriceAlertDb,
    PriceAlertQueryDao.Cache,
    BaseDbImpl<
        PriceAlertChangeEvent,
        PriceAlertRealtime,
        PriceAlertQueryDao,
        PriceAlertInsertDao,
        PriceAlertDeleteDao,
    >() {

  private val queryCache =
      cachify<List<PriceAlert>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryActiveCache =
      cachify<List<PriceAlert>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.queryActive()
      }

  override val deleteDao: PriceAlertDeleteDao = this

  override val insertDao: PriceAlertInsertDao = this

  override val queryDao: PriceAlertQueryDao = this

  override val realtime: PriceAlertRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        queryCache.clear()
        queryActiveCache.clear()
      }

  override suspend fun invalidateActive() =
      withContext(context = Dispatchers.IO) { queryActiveCache.clear() }

  override suspend fun listenForChanges(onChange: (event: PriceAlertChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) { onEvent(onChange) }

  override suspend fun query(): List<PriceAlert> =
      withContext(context = Dispatchers.IO) { queryCache.call() }

  override suspend fun queryActive(): List<PriceAlert> =
      withContext(context = Dispatchers.IO) { queryActiveCache.call() }

  override suspend fun insert(o: PriceAlert): DbInsert.InsertResult<PriceAlert> =
      withContext(context = Dispatchers.IO) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(PriceAlertChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(PriceAlertChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: PriceAlert, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            invalidate()
            publish(PriceAlertChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}
