package com.pyamsoft.tickertape.portfolio.dig.position.add

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionInsertDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class PositionAddInteractorImpl
@Inject
internal constructor(
    private val positionInsertDao: PositionInsertDao,
) : PositionAddInteractor {

  override suspend fun addNewPosition(
      position: DbPosition
  ): ResultWrapper<DbInsert.InsertResult<DbPosition>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          val result = positionInsertDao.insert(position)
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error inserting new position")
            ResultWrapper.failure(e)
          }
        }
      }
}
