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

package com.pyamsoft.tickertape.db.position

import com.pyamsoft.cachify.cachify
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.db.BaseDbImpl
import com.pyamsoft.tickertape.db.DbApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class PositionDbImpl
@Inject
internal constructor(
    @DbApi realQueryDao: PositionQueryDao,
    @param:DbApi private val realInsertDao: PositionInsertDao,
    @param:DbApi private val realDeleteDao: PositionDeleteDao,
) :
    BaseDbImpl<
        PositionChangeEvent,
        PositionRealtime,
        PositionQueryDao,
        PositionInsertDao,
        PositionDeleteDao>(),
    PositionDb {

  private val queryCache =
      cachify<List<DbPosition>> {
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query(true)
      }

  override fun realtime(): PositionRealtime {
    return this
  }

  override fun queryDao(): PositionQueryDao {
    return this
  }

  override fun insertDao(): PositionInsertDao {
    return this
  }

  override fun deleteDao(): PositionDeleteDao {
    return this
  }

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
      }

  override suspend fun listenForChanges(onChange: suspend (event: PositionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(force: Boolean): List<DbPosition> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        if (force) {
          invalidate()
        }

        return@withContext queryCache.call()
      }

  override suspend fun insert(o: DbPosition): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realInsertDao.insert(o).also { inserted ->
          if (inserted) {
            publish(PositionChangeEvent.Insert(o))
          } else {
            publish(PositionChangeEvent.Update(o))
          }
        }
      }

  override suspend fun delete(o: DbPosition, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            publish(PositionChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}