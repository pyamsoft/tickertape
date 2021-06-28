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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.alert.params.RefreshParameters
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class RefresherRunner
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val quoteInteractor: QuoteInteractor,
    private val tapeLauncher: TapeLauncher
) : BaseRunner<RefreshParameters>() {

  // TODO Same code as in WatchlistInteractor, common somehow?
  @CheckResult
  private suspend fun getSymbols(force: Boolean): List<StockSymbol> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext symbolQueryDao.query(force).map { it.symbol() }
      }

  override suspend fun performWork(params: RefreshParameters) = coroutineScope {
    val force = params.forceRefresh

    val symbols = getSymbols(force)
    quoteInteractor.getQuotes(force, symbols).onSuccess { tapeLauncher.start() }.onFailure {
      Timber.e(it, "Error refreshing quotes")
    }

    return@coroutineScope
  }
}
