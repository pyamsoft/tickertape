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

package com.pyamsoft.tickertape.portfolio.dig.splits.add

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitInsertDao
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SplitAddInteractorImpl
@Inject
internal constructor(
    private val splitQueryDao: SplitQueryDao,
    private val splitInsertDao: SplitInsertDao,
) : SplitAddInteractor {

  override suspend fun submitSplit(split: DbSplit): ResultWrapper<DbInsert.InsertResult<DbSplit>> =
      withContext(context = Dispatchers.IO) {
        try {
          val result = splitInsertDao.insert(split)
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error inserting or updating split")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun loadExistingSplit(id: DbSplit.Id): ResultWrapper<DbSplit> =
      withContext(context = Dispatchers.IO) {
        try {
          when (val result = splitQueryDao.queryById(id)) {
            is Maybe.Data -> ResultWrapper.success(result.data)
            is Maybe.None -> ResultWrapper.failure(RuntimeException("No split for id: ${id.raw}"))
          }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error querying for split with ID: $id")
            ResultWrapper.failure(e)
          }
        }
      }
}
