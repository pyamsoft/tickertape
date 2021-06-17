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
class WatchlistAddInteractor
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolInsertDao: SymbolInsertDao,
) {

  @CheckResult
  suspend fun commitSymbol(symbols: List<StockSymbol>) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // TODO move this query into the DAO layer
        for (symbol in symbols) {
          val existingDbSymbol = symbolQueryDao.query(true).find { it.symbol() == symbol }
          if (existingDbSymbol != null) {
            Timber.d("Symbol already exists in DB: $existingDbSymbol")
            continue
          }

          val newSymbol = JsonMappableDbSymbol.create(symbol)
          Timber.d("Insert new symbol into DB: $newSymbol")
          symbolInsertDao.insert(newSymbol)
        }
      }
}
