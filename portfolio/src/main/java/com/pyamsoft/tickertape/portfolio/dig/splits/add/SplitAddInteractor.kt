package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.split.DbSplit

interface SplitAddInteractor {

  @CheckResult suspend fun loadExistingSplit(id: DbSplit.Id): ResultWrapper<DbSplit>

  @CheckResult
  suspend fun submitSplit(split: DbSplit): ResultWrapper<DbInsert.InsertResult<DbSplit>>
}
