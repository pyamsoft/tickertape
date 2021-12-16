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

package com.pyamsoft.tickertape.portfolio.dig

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.dig.DigInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class PortfolioDigInteractorImpl
@Inject
internal constructor(
    interactor: TickerInteractor,
    private val holdingQueryDao: HoldingQueryDao,
    private val positionQueryDao: PositionQueryDao,
) : DigInteractorImpl(interactor), PortfolioDigInteractor {

  override suspend fun getHolding(
      force: Boolean,
      id: DbHolding.Id,
  ): ResultWrapper<DbHolding> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val holding = holdingQueryDao.query(force).firstOrNull { it.id() == id }
          ResultWrapper.success(holding.requireNotNull { "Unable to find holding with id: $id" })
        } catch (e: Throwable) {
          Timber.e(e, "Failed to get db holding: $id")
          ResultWrapper.failure(e)
        }
      }

  override suspend fun getPositions(
      force: Boolean,
      id: DbHolding.Id,
  ): ResultWrapper<List<DbPosition>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val positions = positionQueryDao.query(force).filter { it.holdingId() == id }
          ResultWrapper.success(positions)
        } catch (e: Throwable) {
          Timber.e(e, "Failed to get db positions: $id")
          ResultWrapper.failure(e)
        }
      }
}
