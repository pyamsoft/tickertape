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

package com.pyamsoft.tickertape.db.mover

import com.pyamsoft.cachify.cachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class BigMoverDbImpl
@Inject
internal constructor(
    @DbApi realQueryDao: BigMoverQueryDao,
    @param:DbApi private val realInsertDao: BigMoverInsertDao,
    @param:DbApi private val realDeleteDao: BigMoverDeleteDao,
) :
    BaseDbImpl<
        BigMoverChangeEvent,
        BigMoverRealtime,
        BigMoverQueryDao,
        BigMoverInsertDao,
        BigMoverDeleteDao,
    >(),
    BigMoverDb {

  private val queryCache =
      cachify<List<BigMoverReport>> {
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query(true)
      }

  override fun realtime(): BigMoverRealtime {
    return this
  }

  override fun queryDao(): BigMoverQueryDao {
    return this
  }

  override fun insertDao(): BigMoverInsertDao {
    return this
  }

  override fun deleteDao(): BigMoverDeleteDao {
    return this
  }

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
      }

  override suspend fun listenForChanges(onChange: suspend (event: BigMoverChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(force: Boolean): List<BigMoverReport> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        if (force) {
          invalidate()
        }

        return@withContext queryCache.call()
      }

  override suspend fun insert(o: BigMoverReport): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realInsertDao.insert(o).also { inserted ->
          if (inserted) {
            publish(BigMoverChangeEvent.Insert(o))
          } else {
            publish(BigMoverChangeEvent.Update(o))
          }
        }
      }

  override suspend fun delete(o: BigMoverReport, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            publish(BigMoverChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}
