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

package com.pyamsoft.tickertape.portfolio.manage.positions

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
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
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class PositionsInteractor
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
      numberOfShares: StockShareValue,
      pricePerShare: StockMoneyValue
  ): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val holding = holdingQueryDao.query(false).firstOrNull { it.id() == id }
          if (holding == null) {
            val err =
                IllegalStateException(
                    "Cannot create position for invalid holding: $id $numberOfShares shares at ${pricePerShare.asMoneyValue()}")
            Timber.e(err)
            return@withContext ResultWrapper.failure(err)
          }

          val position =
              JsonMappableDbPosition.create(
                  holdingId = id, shareCount = numberOfShares, price = pricePerShare)

          Timber.d("Insert new position into DB: $position")
          ResultWrapper.success(positionInsertDao.insert(position))
        } catch (e: Throwable) {
          Timber.e(e, "Error creating position $id $numberOfShares $pricePerShare")
          ResultWrapper.failure(e)
        }
      }

  @CheckResult
  suspend fun getHolding(force: Boolean, id: DbHolding.Id): ResultWrapper<PortfolioStock> =
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
          val err = IllegalStateException("Could not find holding with ID: $id")
          Timber.w(err)
          return@withContext ResultWrapper.failure(err)
        }

        // We can cast since we know what this one is
        @Suppress("UNCHECKED_CAST") val positions = jobResult[1] as List<DbPosition>
        return@withContext interactor
            .getQuotes(force, listOf(holding.symbol()))
            .onFailure { Timber.e(it, "Unable to get quotes for holding: $holding") }
            .recover { emptyList() }
            .map { quotes -> quotes.firstOrNull { it.symbol == holding.symbol() } }
            .map { PortfolioStock(holding = holding, positions = positions, quote = it) }
      }

  @CheckResult
  suspend fun removePosition(id: DbPosition.Id): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          // TODO move this query into the DAO layer
          val dbPosition = positionQueryDao.query(true).firstOrNull { it.id() == id }
          if (dbPosition == null) {
            val err = IllegalStateException("Position does not exist in DB: $id")
            Timber.e(err)
            return@withContext ResultWrapper.failure(err)
          }

          ResultWrapper.success(positionDeleteDao.delete(dbPosition, offerUndo = true))
        } catch (e: Throwable) {
          Timber.e(e, "Error removing position: $id")
          ResultWrapper.failure(e)
        }
      }
}
