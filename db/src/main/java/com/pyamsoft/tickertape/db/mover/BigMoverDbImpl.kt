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

package com.pyamsoft.tickertape.db.mover

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
internal class BigMoverDbImpl
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    @DbApi realQueryDao: BigMoverQueryDao,
    @DbApi private val realInsertDao: BigMoverInsertDao,
    @DbApi private val realDeleteDao: BigMoverDeleteDao,
) :
    BigMoverDb,
    BigMoverQueryDao.Cache,
    BaseDbImpl<
        BigMoverChangeEvent,
        BigMoverRealtime,
        BigMoverQueryDao,
        BigMoverInsertDao,
        BigMoverDeleteDao,
    >() {

  private val queryCache =
      cachify<List<BigMoverReport>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  override val deleteDao: BigMoverDeleteDao = this

  override val insertDao: BigMoverInsertDao = this

  override val queryDao: BigMoverQueryDao = this

  override val realtime: BigMoverRealtime = this

  override suspend fun invalidate() = withContext(context = Dispatchers.IO) { queryCache.clear() }

  override suspend fun listenForChanges(onChange: (event: BigMoverChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) { onEvent(onChange) }

  override suspend fun query(): List<BigMoverReport> =
      withContext(context = Dispatchers.IO) { queryCache.call() }

  override suspend fun insert(o: BigMoverReport): DbInsert.InsertResult<BigMoverReport> =
      withContext(context = Dispatchers.IO) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(BigMoverChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(BigMoverChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: BigMoverReport, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            invalidate()
            publish(BigMoverChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}
