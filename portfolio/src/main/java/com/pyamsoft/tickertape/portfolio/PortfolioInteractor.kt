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
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingChangeEvent
import com.pyamsoft.tickertape.db.holding.HoldingDeleteDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.holding.HoldingRealtime
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.position.PositionRealtime
import com.pyamsoft.tickertape.quote.QuoteInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class PortfolioInteractor
@Inject
internal constructor(
    private val holdingRealtime: HoldingRealtime,
    private val positionRealtime: PositionRealtime,
    private val positionQueryDao: PositionQueryDao,
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingDeleteDao: HoldingDeleteDao,
    private val interactor: QuoteInteractor
) {

  suspend fun listenForHoldingChanges(onChange: suspend (event: HoldingChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        holdingRealtime.listenForChanges(onChange)
      }

  suspend fun listenForPositionChanges(onChange: suspend (event: PositionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        positionRealtime.listenForChanges(onChange)
      }

  @CheckResult
  suspend fun getPortfolio(force: Boolean): List<PortfolioStock> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // Run both queries in parallel
        val holdingQueryJob = async { holdingQueryDao.query(force) }
        val positionQueryJob = async { positionQueryDao.query(force) }
        val jobResult = awaitAll(holdingQueryJob, positionQueryJob)

        // We can cast since we know what this one is
        @Suppress("UNCHECKED_CAST") val holdings = jobResult[0] as List<DbHolding>
        val symbols = holdings.map { it.symbol() }

        // We can cast since we know what this one is
        @Suppress("UNCHECKED_CAST") val positions = jobResult[1] as List<DbPosition>
        val quotes =
            try {
              interactor.getQuotes(force, symbols)
            } catch (e: Throwable) {
              Timber.e(e, "Unable to get quotes for portfolio: $symbols")
              emptyList()
            }

        val result = mutableListOf<PortfolioStock>()
        for (holding in holdings) {
          val quote = quotes.firstOrNull { it.symbol == holding.symbol() }
          val holdingPositions = positions.filter { it.holdingId() == holding.id() }
          result.add(PortfolioStock(holding = holding, positions = holdingPositions, quote = quote))
        }

        return@withContext result
      }

  @CheckResult
  suspend fun removeHolding(id: DbHolding.Id) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // TODO move this query into the DAO layer
        val dbHolding = holdingQueryDao.query(true).find { it.id() == id }
        if (dbHolding == null) {
          Timber.d("Holding does not exist in DB: $id")
          return@withContext
        }

        holdingDeleteDao.delete(dbHolding, offerUndo = true)
      }
}