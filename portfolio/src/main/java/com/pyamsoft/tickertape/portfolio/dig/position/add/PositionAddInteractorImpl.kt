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

package com.pyamsoft.tickertape.portfolio.dig.position.add

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionInsertDao
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class PositionAddInteractorImpl
@Inject
internal constructor(
    private val positionQueryDao: PositionQueryDao,
    private val positionInsertDao: PositionInsertDao,
) : PositionAddInteractor {

  override suspend fun submitPosition(
      position: DbPosition
  ): ResultWrapper<DbInsert.InsertResult<DbPosition>> =
      withContext(context = Dispatchers.IO) {
        try {
          val result = positionInsertDao.insert(position)
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error inserting or updating position")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun loadExistingPosition(id: DbPosition.Id): ResultWrapper<DbPosition> =
      withContext(context = Dispatchers.IO) {
        try {
          val result = positionQueryDao.query().first { it.id == id }
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error querying for position with ID: $id")
            ResultWrapper.failure(e)
          }
        }
      }
}
