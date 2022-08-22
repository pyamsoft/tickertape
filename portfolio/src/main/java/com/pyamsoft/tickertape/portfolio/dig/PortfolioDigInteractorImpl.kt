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
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.position.PositionDeleteDao
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.position.PositionRealtime
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.db.split.SplitDeleteDao
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import com.pyamsoft.tickertape.db.split.SplitRealtime
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.dig.DigInteractorImpl
import com.pyamsoft.tickertape.stocks.StockInteractor
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
    interactorCache: TickerInteractor.Cache,
    stockInteractor: StockInteractor,
    stockInteractorCache: StockInteractor.Cache,
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingQueryDaoCache: HoldingQueryDao.Cache,
    private val positionQueryDao: PositionQueryDao,
    private val positionQueryDaoCache: PositionQueryDao.Cache,
    private val positionRealtime: PositionRealtime,
    private val positionDeleteDao: PositionDeleteDao,
    private val splitQueryDao: SplitQueryDao,
    private val splitQueryDaoCache: SplitQueryDao.Cache,
    private val splitRealtime: SplitRealtime,
    private val splitDeleteDao: SplitDeleteDao,
) :
    PortfolioDigInteractor,
    PortfolioDigInteractor.Cache,
    DigInteractorImpl(
        interactor,
        interactorCache,
        stockInteractor,
        stockInteractorCache,
    ) {

  override suspend fun deletePosition(position: DbPosition): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(positionDeleteDao.delete(position, offerUndo = true))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to delete position: $position")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun deleteSplit(split: DbSplit): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(splitDeleteDao.delete(split, offerUndo = true))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to delete split: $split")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun watchSplits(onEvent: (SplitChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext splitRealtime.listenForChanges(onEvent)
      }

  override suspend fun watchPositions(onEvent: (PositionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext positionRealtime.listenForChanges(onEvent)
      }

  override suspend fun getHolding(id: DbHolding.Id): ResultWrapper<DbHolding> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val holding = holdingQueryDao.query().firstOrNull { it.id == id }
          ResultWrapper.success(holding.requireNotNull { "Unable to find holding with id: $id" })
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to get db holding: $id")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidateHolding() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        holdingQueryDaoCache.invalidate()
      }

  override suspend fun getPositions(id: DbHolding.Id): ResultWrapper<List<DbPosition>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val positions = positionQueryDao.query().filter { it.holdingId == id }
          ResultWrapper.success(positions)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to get db positions: $id")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidatePositions() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        positionQueryDaoCache.invalidate()
      }

  override suspend fun getSplits(id: DbHolding.Id): ResultWrapper<List<DbSplit>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val positions = splitQueryDao.query().filter { it.holdingId == id }
          ResultWrapper.success(positions)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to get db splits: $id")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidateSplits() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        splitQueryDaoCache.invalidate()
      }
}
