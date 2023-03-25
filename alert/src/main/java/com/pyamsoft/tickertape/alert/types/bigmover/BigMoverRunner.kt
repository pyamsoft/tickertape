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

package com.pyamsoft.tickertape.alert.types.bigmover

import com.pyamsoft.tickertape.alert.base.BaseRunner
import com.pyamsoft.tickertape.db.getQuotesForHoldings
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

@Singleton
internal class BigMoverRunner
@Inject
internal constructor(
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingQueryDaoCache: HoldingQueryDao.Cache,
    private val stockInteractor: StockInteractor,
    private val stockInteractorCache: StockInteractor.Cache,
    private val standalone: BigMoverStandalone,
) : BaseRunner<BigMoverWorkerParameters>() {

  override suspend fun performWork(params: BigMoverWorkerParameters) = coroutineScope {
    val force = params.forceRefresh
    try {
      // Don't use TickerInteractor here since this is imported in TickerInteractor, which would
      // make a circular dependency
      if (force) {
        holdingQueryDaoCache.invalidate()
        stockInteractorCache.invalidateAllQuotes()
      }

      val quotes = stockInteractor.getQuotesForHoldings(holdingQueryDao)
      standalone.notifyForBigMovers(quotes)
    } catch (e: Throwable) {
      Timber.e(e, "Error getting watchlist quotes for big movers")
    }
  }
}
