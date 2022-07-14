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

package com.pyamsoft.tickertape.portfolio

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.holding.HoldingDeleteDao
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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class PortfolioInteractorImpl
@Inject
internal constructor(
    private val holdingRealtime: HoldingRealtime,
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingDeleteDao: HoldingDeleteDao,
    private val positionRealtime: PositionRealtime,
    private val positionQueryDao: PositionQueryDao,
    private val splitRealtime: SplitRealtime,
    private val splitQueryDao: SplitQueryDao,
    private val interactor: TickerInteractor,
) : PortfolioInteractor {

  override suspend fun listenForHoldingChanges(onChange: (event: HoldingChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        holdingRealtime.listenForChanges(onChange)
      }

  override suspend fun listenForPositionChanges(onChange: (event: PositionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        positionRealtime.listenForChanges(onChange)
      }

  override suspend fun listenForSplitChanges(onChange: (event: SplitChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        splitRealtime.listenForChanges(onChange)
      }

  override suspend fun getPortfolio(force: Boolean): ResultWrapper<List<PortfolioStock>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          coroutineScope {
            // Run both queries in parallel
            val jobResult =
                awaitAll(
                    async { holdingQueryDao.query(force) },
                    async { positionQueryDao.query(force) },
                    async { splitQueryDao.query(force) },
                )

            // We can cast since we know what this one is
            @Suppress("UNCHECKED_CAST") val holdings = jobResult[0] as List<DbHolding>
            val symbols = holdings.map { it.symbol() }

            // We can cast since we know what this one is
            @Suppress("UNCHECKED_CAST") val positions = jobResult[1] as List<DbPosition>

            // We can cast since we know what this one is
            @Suppress("UNCHECKED_CAST") val splits = jobResult[2] as List<DbSplit>

            return@coroutineScope interactor
                .getQuotes(
                    force,
                    symbols,
                    options = TICKER_OPTIONS,
                )
                .onFailure { Timber.e(it, "Unable to get quotes for portfolio: $symbols") }
                .recover { emptyList() }
                .map { quotes ->
                  val result = mutableListOf<PortfolioStock>()
                  for (holding in holdings) {
                    val quote = quotes.firstOrNull { it.symbol == holding.symbol() }
                    val holdingPositions = positions.filter { it.holdingId() == holding.id() }
                    val holdingSplits = splits.filter { it.holdingId() == holding.id() }
                    val stock =
                        PortfolioStock(
                            holding = holding,
                            positions = holdingPositions,
                            ticker = quote,
                            splits = holdingSplits,
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

  @CheckResult
  override suspend fun removeHolding(id: DbHolding.Id): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          // TODO move this query into the DAO layer
          val holding = holdingQueryDao.query(true).firstOrNull { it.id() == id }
          if (holding == null) {
            val err = IllegalStateException("Holding does not exist in DB: $id")
            Timber.e(err)
            return@withContext ResultWrapper.failure(err)
          }

          ResultWrapper.success(holdingDeleteDao.delete(holding, offerUndo = true))
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
