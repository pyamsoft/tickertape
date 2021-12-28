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

package com.pyamsoft.tickertape.watchlist.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.symbol.JsonMappableDbSymbol
import com.pyamsoft.tickertape.db.symbol.SymbolInsertDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WatchlistAddInteractor
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolInsertDao: SymbolInsertDao,
) {

  @CheckResult
  suspend fun commitSymbol(symbol: StockSymbol): ResultWrapper<Unit> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          // TODO move this query into the DAO layer
          val existingDbSymbol = symbolQueryDao.query(true).firstOrNull { it.symbol() == symbol }
          if (existingDbSymbol != null) {
            Timber.d("Symbol already exists in DB: $existingDbSymbol")
            return@withContext ResultWrapper.success(Unit)
          }

          val newSymbol = JsonMappableDbSymbol.create(symbol)
          return@withContext symbolInsertDao.insert(newSymbol).run { ResultWrapper.success(Unit) }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error committing symbol: $symbol")
            ResultWrapper.failure(e)
          }
        }
      }
}
