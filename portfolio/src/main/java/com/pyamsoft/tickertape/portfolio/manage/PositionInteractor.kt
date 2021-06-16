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

package com.pyamsoft.tickertape.portfolio.manage

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.JsonMappableDbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.position.PositionDeleteDao
import com.pyamsoft.tickertape.db.position.PositionInsertDao
import com.pyamsoft.tickertape.db.position.PositionQueryDao
import com.pyamsoft.tickertape.db.position.PositionRealtime
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class PositionInteractor
@Inject
internal constructor(
    private val positionRealtime: PositionRealtime,
    private val positionQueryDao: PositionQueryDao,
    private val positionDeleteDao: PositionDeleteDao,
    private val positionInsertDao: PositionInsertDao,
    private val holdingQueryDao: HoldingQueryDao,
    private val interactor: QuoteInteractor
) {

  suspend fun listenForPositionChanges(onChange: suspend (event: PositionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        positionRealtime.listenForChanges(onChange)
      }

  @CheckResult
  suspend fun createPosition(
      id: DbHolding.Id,
      numberOfShares: Int,
      pricePerShare: StockMoneyValue
  ) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val holding = holdingQueryDao.query(false).firstOrNull { it.id() == id }
        if (holding == null) {
          Timber.w(
              "Cannot create position for invalid holding: $id $numberOfShares shares at ${pricePerShare.asMoneyValue()}")
          return@withContext
        }

        val position =
            JsonMappableDbPosition.create(
                holdingId = id,
                shareCount = numberOfShares,
                fractionalShareCount = 0F,
                price = pricePerShare)

        Timber.d("Insert new position into DB: $position")
        positionInsertDao.insert(position)
      }

  @CheckResult
  suspend fun getHolding(force: Boolean, id: DbHolding.Id): PortfolioStock? =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // Run both queries in parallel
        val holdingQueryJob = async { holdingQueryDao.query(force).firstOrNull { it.id() == id } }
        val positionQueryJob = async {
          positionQueryDao.query(force).filter { it.holdingId() == id }
        }
        val jobResult = awaitAll(holdingQueryJob, positionQueryJob)

        // We can cast since we know what this one is
        @Suppress("UNCHECKED_CAST") val holding = jobResult[0] as? DbHolding
        if (holding == null) {
          Timber.w("Could not find holding with ID: $id")
          return@withContext null
        }

        // We can cast since we know what this one is
        @Suppress("UNCHECKED_CAST") val positions = jobResult[1] as List<DbPosition>
        val quotes =
            try {
              interactor.getQuotes(force, listOf(holding.symbol()))
            } catch (e: Throwable) {
              Timber.e(e, "Unable to get quotes for holding: $holding")
              emptyList()
            }

        val quote = quotes.firstOrNull { it.symbol == holding.symbol() }
        return@withContext PortfolioStock(holding = holding, positions = positions, quote = quote)
      }

  @CheckResult
  suspend fun removePosition(id: DbPosition.Id) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // TODO move this query into the DAO layer
        val dbPosition = positionQueryDao.query(true).find { it.id() == id }
        if (dbPosition == null) {
          Timber.d("Position does not exist in DB: $id")
          return@withContext
        }

        positionDeleteDao.delete(dbPosition, offerUndo = true)
      }
}
