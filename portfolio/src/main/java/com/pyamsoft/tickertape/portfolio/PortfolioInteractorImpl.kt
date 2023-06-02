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

package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.holding.HoldingDeleteDao
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.holding.HoldingRealtime
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.position.PositionRealtime
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.db.split.SplitQueryDao
import com.pyamsoft.tickertape.db.split.SplitRealtime
import com.pyamsoft.tickertape.quote.TickerInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PortfolioInteractorImpl
@Inject
internal constructor(
    private val holdingRealtime: HoldingRealtime,
    private val holdingInsertDao: HoldingInsertDao,
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingQueryDaoCache: HoldingQueryDao.Cache,
    private val holdingDeleteDao: HoldingDeleteDao,
    private val positionRealtime: PositionRealtime,
    private val positionQueryDao: PositionQueryDao,
    private val positionQueryDaoCache: PositionQueryDao.Cache,
    private val splitRealtime: SplitRealtime,
    private val splitQueryDao: SplitQueryDao,
    private val splitQueryDaoCache: SplitQueryDao.Cache,
    private val interactor: TickerInteractor,
    private val interactorCache: TickerInteractor.Cache,
    private val clock: Clock,
) : PortfolioInteractor, PortfolioInteractor.Cache {

  override suspend fun restoreHolding(
      holding: DbHolding
  ): ResultWrapper<DbInsert.InsertResult<DbHolding>> =
      withContext(context = Dispatchers.Default) {
        try {
          ResultWrapper.success(holdingInsertDao.insert(holding))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to restore holding: $holding")
            ResultWrapper.failure(e)
          }
        }
      }

  override fun listenForHoldingChanges(): Flow<HoldingChangeEvent> {
    return holdingRealtime.listenForChanges()
  }

  override fun listenForPositionChanges(): Flow<PositionChangeEvent> {
    return positionRealtime.listenForChanges()
  }

  override fun listenForSplitChanges(): Flow<SplitChangeEvent> {
    return splitRealtime.listenForChanges()
  }

  override suspend fun getPortfolio(): ResultWrapper<List<PortfolioStock>> =
      withContext(context = Dispatchers.Default) {
        try {
          coroutineScope {
            // Run both queries in parallel
            val jobResult =
                awaitAll(
                    async { holdingQueryDao.query() },
                    async { positionQueryDao.query() },
                    async { splitQueryDao.query() },
                )

            // We can cast since we know what this one is
            @Suppress("UNCHECKED_CAST") val holdings = jobResult[0] as List<DbHolding>
            val symbols = holdings.map { it.symbol }

            // We can cast since we know what this one is
            @Suppress("UNCHECKED_CAST") val positions = jobResult[1] as List<DbPosition>

            // We can cast since we know what this one is
            @Suppress("UNCHECKED_CAST") val splits = jobResult[2] as List<DbSplit>

            return@coroutineScope interactor
                .getQuotes(
                    symbols,
                    options = TICKER_OPTIONS,
                )
                .onFailure { Timber.e(it, "Unable to get quotes for portfolio: $symbols") }
                .recover { emptyList() }
                .map { quotes ->
                  val result = mutableListOf<PortfolioStock>()
                  for (holding in holdings) {
                    val quote = quotes.firstOrNull { it.symbol == holding.symbol }
                    val holdingPositions = positions.filter { it.holdingId == holding.id }
                    val holdingSplits = splits.filter { it.holdingId == holding.id }
                    val stock =
                        PortfolioStock(
                            holding = holding,
                            positions = holdingPositions,
                            ticker = quote,
                            splits = holdingSplits,
                            clock = clock,
                        )
                    result.add(stock)
                  }

                  return@map result
                }
          }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to get quotes for portfolio")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidatePortfolio() =
      withContext(context = Dispatchers.Default) {
        // Clear all DB
        awaitAll(
            async { holdingQueryDaoCache.invalidate() },
            async { positionQueryDaoCache.invalidate() },
            async { splitQueryDaoCache.invalidate() },
        )

        // Clear cache for quotes
        interactorCache.invalidateAllQuotes()
      }

  @CheckResult
  override suspend fun removeHolding(id: DbHolding.Id): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.Default) {
        try {
          // First invalidate cache to be sure we are up to date
          holdingQueryDaoCache.invalidateByHoldingId(id)

          when (val holding = holdingQueryDao.queryById(id)) {
            is Maybe.Data -> {
              ResultWrapper.success(holdingDeleteDao.delete(holding.data, offerUndo = true))
            }
            is Maybe.None -> {
              val err = IllegalStateException("Holding does not exist in DB: $id")
              Timber.e(err)
              return@withContext ResultWrapper.failure(err)
            }
          }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error removing holding $id")
            ResultWrapper.failure(e)
          }
        }
      }

  companion object {
    private val TICKER_OPTIONS =
        TickerInteractor.Options(
            notifyBigMovers = true,
        )
  }
}
