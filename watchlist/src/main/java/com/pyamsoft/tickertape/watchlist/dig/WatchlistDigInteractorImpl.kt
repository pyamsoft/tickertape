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

package com.pyamsoft.tickertape.watchlist.dig

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.symbol.DbSymbol
import com.pyamsoft.tickertape.db.symbol.JsonMappableDbSymbol
import com.pyamsoft.tickertape.db.symbol.SymbolDeleteDao
import com.pyamsoft.tickertape.db.symbol.SymbolInsertDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.dig.DigInteractorImpl
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WatchlistDigInteractorImpl
@Inject
internal constructor(
    interactor: TickerInteractor,
    stockInteractor: StockInteractor,
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolDeleteDao: SymbolDeleteDao,
    private val symbolInsertDao: SymbolInsertDao,
) : DigInteractorImpl(interactor, stockInteractor), WatchlistDigInteractor {

  @CheckResult
  private suspend fun getExistingSymbol(force: Boolean, symbol: StockSymbol): DbSymbol? {
    return symbolQueryDao.query(force).firstOrNull { it.symbol == symbol }
  }

  override suspend fun isInWatchlist(symbol: StockSymbol, force: Boolean): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
          ResultWrapper.success(getExistingSymbol(force, symbol) != null)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error checking if symbol in watchlist: $symbol")
            ResultWrapper.failure(e)
          }
        }
      }

  /**
   * NOTE: Bad API
   *
   * A return of True
   * - "was added to watchlist"
   *
   * A result of False
   * - "Was removed from watchlist"
   */
  override suspend fun modifyWatchlist(symbol: StockSymbol): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val existingSymbol = getExistingSymbol(force = true, symbol)
        return@withContext try {
          if (existingSymbol == null) {
            Timber.d("Insert symbol into watchlist: $symbol")
            when (val result = symbolInsertDao.insert(JsonMappableDbSymbol.create(symbol))) {
              is DbInsert.InsertResult.Fail -> ResultWrapper.failure(result.error)
              else -> ResultWrapper.success(true)
            }
          } else {
            Timber.d("Remove symbol from watchlist: $symbol")
            val deleted = symbolDeleteDao.delete(existingSymbol, false)

            // If deleted is true, this will return False, which is what this function wants to see
            ResultWrapper.success(!deleted)
          }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error modifying watchlist")
            ResultWrapper.failure(e)
          }
        }
      }
}
