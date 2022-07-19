package com.pyamsoft.tickertape.portfolio.dig.splits.add

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
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
        Enforcer.assertOffMainThread()
        return@withContext try {
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
        Enforcer.assertOffMainThread()
        return@withContext try {
          val result = splitQueryDao.query(false).first { it.id == id }
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error querying for split with ID: $id")
            ResultWrapper.failure(e)
          }
        }
      }
}
