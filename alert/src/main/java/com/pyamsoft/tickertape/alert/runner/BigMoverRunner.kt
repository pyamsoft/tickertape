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

package com.pyamsoft.tickertape.alert.runner

import com.pyamsoft.tickertape.alert.params.BigMoverParameters
import com.pyamsoft.tickertape.alert.standalone.BigMoverStandalone
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

@Singleton
internal class BigMoverRunner
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val stockInteractor: StockInteractor,
    private val standalone: BigMoverStandalone,
) : BaseRunner<BigMoverParameters>() {

  override suspend fun performWork(params: BigMoverParameters) = coroutineScope {
    val force = params.forceRefresh
    try {
      // Don't use TickerInteractor here since this is imported in TickerInteractor, which would
      // make a circular dependency
      val watchList = symbolQueryDao.query(force).map { it.symbol() }
      val quotes = stockInteractor.getQuotes(force, watchList)
      standalone.notifyForBigMovers(quotes)
    } catch (e: Throwable) {
      Timber.e(e, "Error getting watchlist quotes for big movers")
    }
  }
}
