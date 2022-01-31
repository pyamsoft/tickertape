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

package com.pyamsoft.tickertape.stocks.sources.yf

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.InternalApi
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.data.KeyStatisticsImpl
import com.pyamsoft.tickertape.stocks.network.NetworkKeyStatisticsResponse
import com.pyamsoft.tickertape.stocks.service.KeyStatisticsService
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import javax.inject.Inject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

internal class YahooKeyStatisticsSource
@Inject
internal constructor(@InternalApi private val service: KeyStatisticsService) : KeyStatisticSource {

  @CheckResult
  private suspend fun fetchKeyStatistics(symbol: StockSymbol): PairedResponse {
    val response =
        service.getStatistics(
            symbol = symbol.symbol(),
            modules = ALL_MODULES_STRING,
        )
    return PairedResponse(
        symbol = symbol,
        response = response,
    )
  }

  override suspend fun getKeyStatistics(
      force: Boolean,
      symbols: List<StockSymbol>
  ): List<KeyStatistics> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (symbols.isEmpty()) {
          return@withContext emptyList()
        }

        return@withContext coroutineScope {
          val jobs = mutableListOf<Deferred<PairedResponse>>()
          for (symbol in symbols) {
            jobs.add(async { fetchKeyStatistics(symbol) })
          }

          return@coroutineScope jobs.awaitAll().map { paired ->
            return@map KeyStatisticsImpl(
                symbol = paired.symbol,
            )
          }
        }
      }

  companion object {

    private val ALL_MODULES_STRING = YFModules.values().joinToString(separator = ",") { it.module }

    private data class PairedResponse(
        val symbol: StockSymbol,
        val response: NetworkKeyStatisticsResponse,
    )

    private enum class YFModules(internal val module: String) {
      FINANCIAL_DATA("financialData"),
      KEY_STATISTICS("defaultKeyStatistics"),
      CALENDAR_EVENTS("calendarEvents"),
      INCOME_HISTORY("incomeStatementHistory"),
      CASHFLOW_HISTORY("cashflowStatementHistory"),
      BALANCE_SHEET_HISTORY("balanceSheetHistory"),
    }
  }
}
