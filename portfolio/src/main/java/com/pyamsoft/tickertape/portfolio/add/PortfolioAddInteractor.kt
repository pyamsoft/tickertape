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

package com.pyamsoft.tickertape.portfolio.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.holding.JsonMappableDbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class PortfolioAddInteractor
@Inject
internal constructor(
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingInsertDao: HoldingInsertDao,
) {

  @CheckResult
  suspend fun commitSymbol(
      symbol: StockSymbol,
      type: EquityType,
      realEquityType: String,
      side: TradeSide
  ): ResultWrapper<Unit> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          // TODO move this query into the DAO layer
          val existingHolding = holdingQueryDao.query(true).firstOrNull { it.symbol() == symbol }
          if (existingHolding != null) {
            Timber.d("Holding already exists in DB: $existingHolding")
            return@withContext ResultWrapper.success(Unit)
          }

          val newHolding = JsonMappableDbHolding.create(symbol, type, realEquityType, side)
          return@withContext when (holdingInsertDao.insert(newHolding)) {
            DbInsert.InsertResult.INSERT -> Timber.d("New portfolio holding inserted: $newHolding")
            DbInsert.InsertResult.UPDATE ->
                Timber.d("Existing portfolio holding updated: $newHolding")
            DbInsert.InsertResult.FAIL -> Timber.w("Failed to insert/update portfolio holdings")
          }.run { ResultWrapper.success(Unit) }
        } catch (e: Throwable) {
          Timber.e(e, "Error committing symbol: $symbol")
          ResultWrapper.failure(e)
        }
      }
}
