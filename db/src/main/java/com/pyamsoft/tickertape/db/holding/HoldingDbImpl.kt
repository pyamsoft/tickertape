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

package com.pyamsoft.tickertape.db.holding

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
internal class HoldingDbImpl
@Inject
internal constructor(
    @DbApi realQueryDao: HoldingQueryDao,
    @param:DbApi private val realInsertDao: HoldingInsertDao,
    @param:DbApi private val realDeleteDao: HoldingDeleteDao,
) :
    BaseDbImpl<
        HoldingChangeEvent, HoldingRealtime, HoldingQueryDao, HoldingInsertDao, HoldingDeleteDao>(),
    HoldingDb {

  private val queryCache =
      cachify<List<DbHolding>> {
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query(true)
      }

  override val deleteDao: HoldingDeleteDao = this

  override val insertDao: HoldingInsertDao = this

  override val queryDao: HoldingQueryDao = this

  override val realtime: HoldingRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
      }

  override suspend fun listenForChanges(onChange: (event: HoldingChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(force: Boolean): List<DbHolding> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        if (force) {
          invalidate()
        }

        return@withContext queryCache.call()
      }

  override suspend fun insert(o: DbHolding): DbInsert.InsertResult<DbHolding> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> publish(HoldingChangeEvent.Insert(result.data))
            is DbInsert.InsertResult.Update -> publish(HoldingChangeEvent.Update(result.data))
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbHolding, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            publish(HoldingChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}
