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
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.db.symbol.SymbolDeleteDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.db.symbol.SymbolRealtime
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotePair
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class PortfolioInteractor
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolDeleteDao: SymbolDeleteDao,
    private val symbolRealtime: SymbolRealtime,
    private val interactor: QuoteInteractor
) {

  suspend fun listenForChanges(onChange: suspend (event: SymbolChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext symbolRealtime.listenForChanges(onChange)
      }

  @CheckResult
  suspend fun getHoldings(force: Boolean): List<QuotePair> =
      withContext(context = Dispatchers.IO) {
        return@withContext interactor.getQuotes(force)
      }

  @CheckResult
  suspend fun removeHolding(symbol: StockSymbol) =
      withContext(context = Dispatchers.IO) {
        // TODO move this query into the DAO layer
        val dbSymbol = symbolQueryDao.query(true).find { it.symbol().symbol() == symbol.symbol() }
        if (dbSymbol == null) {
          Timber.d("Symbol does not exist in DB: $symbol")
          return@withContext
        }

        symbolDeleteDao.delete(dbSymbol, offerUndo = true)
      }
}
