/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.quote.base

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class BaseTickerInteractorImpl
protected constructor(
    private val stockInteractor: StockInteractor,
    private val stockInteractorCache: StockInteractor.Cache,
) : BaseTickerInteractor, BaseTickerInteractor.Cache {

  final override suspend fun getOptionsChain(
      symbol: StockSymbol,
      expirationDate: LocalDate?,
  ): ResultWrapper<StockOptions> =
      withContext(context = Dispatchers.Default) {
        try {
          val options =
              stockInteractor.getOptions(
                  symbols = listOf(symbol),
                  expirationDate = expirationDate,
              )
          // Right now we only support 1 lookup at a time in the UI
          val result = options.first()
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to lookup options data: $symbol")
            ResultWrapper.failure(e)
          }
        }
      }

  final override suspend fun invalidateOptionsChain(symbol: StockSymbol) =
      withContext(context = Dispatchers.Default) {
        stockInteractorCache.invalidateQuotes(listOf(symbol))
      }
}
