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

package com.pyamsoft.tickertape.quote.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.HoldingInsertDao
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.holding.JsonMappableDbHolding
import com.pyamsoft.tickertape.db.symbol.JsonMappableDbSymbol
import com.pyamsoft.tickertape.db.symbol.SymbolInsertDao
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class NewTickerInteractorImpl
@Inject
internal constructor(
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolInsertDao: SymbolInsertDao,
    private val holdingQueryDao: HoldingQueryDao,
    private val holdingInsertDao: HoldingInsertDao,
    private val stockInteractor: StockInteractor,
    private val stockInteractorCache: StockInteractor.Cache,
    private val tickerInteractor: TickerInteractor,
    private val tickerInteractorCache: TickerInteractor.Cache,
) : NewTickerInteractor, NewTickerInteractor.Cache {

  override suspend fun resolveTicker(symbol: StockSymbol): ResultWrapper<Ticker> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext try {
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
    withContext(context = Dispatchers.IO) {
      Enforcer.assertOffMainThread()
      tickerInteractorCache.invalidateQuotes(listOf(symbol))
    }
  }

  override suspend fun search(query: String): ResultWrapper<List<SearchResult>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
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
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        stockInteractorCache.invalidateSearch(query)
      }

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
  private suspend fun handleInsertWatchlist(
      symbol: StockSymbol,
  ): DbInsert.InsertResult<StockSymbol> {
    val existing = symbolQueryDao.query().firstOrNull { it.symbol == symbol }
    return if (existing == null) {
      val model = JsonMappableDbSymbol.create(symbol)
      val result = symbolInsertDao.insert(model)
      mapResultToSymbol(result) { it.symbol }
    } else {
      val error = IllegalArgumentException("${symbol.raw} already in watchlist")
      DbInsert.InsertResult.Fail(
          data = symbol,
          error = error,
      )
    }
  }

  @CheckResult
  private suspend fun handleInsertPortfolio(
      symbol: StockSymbol,
      equityType: EquityType,
      tradeSide: TradeSide,
  ): DbInsert.InsertResult<StockSymbol> {
    val existing =
        holdingQueryDao.query().firstOrNull { it.symbol == symbol && it.side == tradeSide }
    return if (existing == null) {
      val model =
          JsonMappableDbHolding.create(
              symbol,
              equityType,
              tradeSide,
          )
      val result = holdingInsertDao.insert(model)
      mapResultToSymbol(result) { it.symbol }
    } else {
      val error = IllegalArgumentException("${symbol.raw} already in portfolio: $tradeSide")
      DbInsert.InsertResult.Fail(
          data = symbol,
          error = error,
      )
    }
  }

  override suspend fun insertNewTicker(
      symbol: StockSymbol,
      destination: TickerDestination,
      equityType: EquityType,
      tradeSide: TradeSide,
  ): ResultWrapper<DbInsert.InsertResult<StockSymbol>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val result =
              when (destination) {
                TickerDestination.WATCHLIST -> handleInsertWatchlist(symbol)
                TickerDestination.PORTFOLIO -> handleInsertPortfolio(symbol, equityType, tradeSide)
              }
          ResultWrapper.success(result)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Failed to insert symbol into db: $symbol $destination")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun lookupOptionsData(symbol: StockSymbol): ResultWrapper<StockOptions> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          val options = stockInteractor.getOptions(listOf(symbol))
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

  override suspend fun invalidateOptionsData(symbol: StockSymbol) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        stockInteractorCache.invalidateQuotes(listOf(symbol))
      }

  override suspend fun resolveOptionsIdentifier(
      symbol: StockSymbol,
      expirationDate: LocalDateTime,
      strikePrice: StockMoneyValue,
      contractType: StockOptions.Contract.Type
  ): String =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext stockInteractor.resolveOptionLookupIdentifier(
            symbol = symbol,
            expirationDate = expirationDate.toLocalDate(),
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
