package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.position.DbPosition

interface PositionAddInteractor {

  @CheckResult suspend fun loadExistingPosition(id: DbPosition.Id): ResultWrapper<DbPosition>

  @CheckResult
  suspend fun submitPosition(position: DbPosition): ResultWrapper<DbInsert.InsertResult<DbPosition>>
}
