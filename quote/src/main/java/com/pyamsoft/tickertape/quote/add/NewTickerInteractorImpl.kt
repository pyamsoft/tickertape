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

package com.pyamsoft.tickertape.quote.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.holding.JsonMappableDbHolding
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.base.BaseTickerInteractorImpl
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class NewTickerInteractorImpl
@Inject
internal constructor(
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingInsertDao: HoldingInsertDao,
    private val stockInteractor: StockInteractor,
    private val stockInteractorCache: StockInteractor.Cache,
    private val tickerInteractor: TickerInteractor,
    private val tickerInteractorCache: TickerInteractor.Cache,
) :
    NewTickerInteractor,
    NewTickerInteractor.Cache,
    BaseTickerInteractorImpl(
        stockInteractor,
        stockInteractorCache,
    ) {

  override suspend fun resolveTicker(symbol: StockSymbol): ResultWrapper<Ticker> =
      withContext(context = Dispatchers.IO) {
        try {
          tickerInteractor
              .getQuotes(
                  listOf(symbol),
                  options = TICKER_OPTIONS,
              )
              // Only pick out the single quote
              .map { list -> list.first { it.symbol == symbol } }
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to resolve ticker: $symbol")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidateTicker(symbol: StockSymbol) {
    withContext(context = Dispatchers.IO) { tickerInteractorCache.invalidateQuotes(listOf(symbol)) }
  }

  override suspend fun search(query: String): ResultWrapper<List<SearchResult>> =
      withContext(context = Dispatchers.IO) {
        try {
          val results = stockInteractor.search(query)
          ResultWrapper.success(results)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to search for '$query'")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun invalidateSearch(query: String) =
      withContext(context = Dispatchers.IO) { stockInteractorCache.invalidateSearch(query) }

  @CheckResult
  private inline fun <T : Any> mapResultToSymbol(
      result: DbInsert.InsertResult<T>,
      mapToSymbol: (T) -> StockSymbol,
  ): DbInsert.InsertResult<StockSymbol> {
    return when (result) {
      is DbInsert.InsertResult.Fail ->
          DbInsert.InsertResult.Fail(
              data = mapToSymbol(result.data),
              error = result.error,
          )
      is DbInsert.InsertResult.Insert ->
          DbInsert.InsertResult.Insert(data = mapToSymbol(result.data))
      is DbInsert.InsertResult.Update ->
          DbInsert.InsertResult.Update(data = mapToSymbol(result.data))
    }
  }

  @CheckResult
  private suspend fun handleInsertPortfolio(
      symbol: StockSymbol,
      equityType: EquityType,
      tradeSide: TradeSide,
  ): DbInsert.InsertResult<StockSymbol> {
    when (holdingQueryDao.queryByTradeSide(symbol, tradeSide)) {
      is Maybe.Data -> {
        val error = IllegalArgumentException("${symbol.raw} already in portfolio: $tradeSide")
        return DbInsert.InsertResult.Fail(
            data = symbol,
            error = error,
        )
      }
      is Maybe.None -> {
        val model =
            JsonMappableDbHolding.create(
                symbol,
                equityType,
                tradeSide,
            )
        val result = holdingInsertDao.insert(model)
        return mapResultToSymbol(result) { it.symbol }
      }
    }
  }

  override suspend fun insertNewTicker(
      symbol: StockSymbol,
      equityType: EquityType,
      tradeSide: TradeSide,
  ): ResultWrapper<DbInsert.InsertResult<StockSymbol>> =
      withContext(context = Dispatchers.IO) {
        try {
          val result = handleInsertPortfolio(symbol, equityType, tradeSide)
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to insert symbol into db: $symbol")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun resolveOptionsIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDate,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type
  ): String =
      withContext(context = Dispatchers.Default) {
        stockInteractor.resolveOptionLookupIdentifier(
            symbol = symbol,
            expirationDate = expirationDate,
            strikePrice = strikePrice,
            contractType = contractType,
        )
      }

  companion object {

    private val TICKER_OPTIONS =
        TickerInteractor.Options(
            notifyBigMovers = true,
        )
  }
}
