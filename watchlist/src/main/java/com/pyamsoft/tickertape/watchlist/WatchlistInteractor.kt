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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class WatchlistInteractor @Inject internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val interactor: StockInteractor
) {

    @CheckResult
    private suspend fun getSymbols(force: Boolean): List<StockSymbol> =
        withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            return@withContext symbolQueryDao.query(force).map { it.symbol() }
        }

    @CheckResult
    suspend fun getQuotes(force: Boolean): List<StockQuote> =
        withContext(context = Dispatchers.IO) {
            return@withContext interactor.getQuotes(force, getSymbols(force))
        }

}
