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

package com.pyamsoft.tickertape.watchlist

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.symbol.SymbolChangeEvent
import com.pyamsoft.tickertape.db.symbol.SymbolDeleteDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.db.symbol.SymbolRealtime
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.getWatchListQuotes
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WatchlistInteractorImpl
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolDeleteDao: SymbolDeleteDao,
    private val symbolRealtime: SymbolRealtime,
    private val interactor: TickerInteractor,
) : WatchlistInteractor {

  override suspend fun listenForChanges(onChange: (event: SymbolChangeEvent) -> Unit) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext symbolRealtime.listenForChanges(onChange)
      }

  override suspend fun getQuotes(force: Boolean): ResultWrapper<List<Ticker>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          interactor.getWatchListQuotes(force, symbolQueryDao, options = TICKER_OPTIONS)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting quotes")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun removeQuote(symbol: StockSymbol): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          // TODO move this query into the DAO layer
          val dbSymbol = symbolQueryDao.query(true).firstOrNull { it.symbol() == symbol }
          if (dbSymbol == null) {
            val err = IllegalStateException("Symbol does not exist in DB: $symbol")
            Timber.e(err)
            return@withContext ResultWrapper.failure(err)
          }

          ResultWrapper.success(symbolDeleteDao.delete(dbSymbol, offerUndo = true))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error removing quote: $symbol")
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
