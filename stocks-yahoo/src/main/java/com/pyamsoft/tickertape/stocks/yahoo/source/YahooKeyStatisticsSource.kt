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

package com.pyamsoft.tickertape.stocks.yahoo.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import com.pyamsoft.tickertape.stocks.yahoo.YahooApi
import com.pyamsoft.tickertape.stocks.yahoo.network.NetworkKeyStatisticsResponse
import com.pyamsoft.tickertape.stocks.yahoo.network.asDataPoint
import com.pyamsoft.tickertape.stocks.yahoo.service.KeyStatisticsService
import javax.inject.Inject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class YahooKeyStatisticsSource
@Inject
internal constructor(@YahooApi private val service: KeyStatisticsService) : KeyStatisticSource {

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
            return@map KeyStatistics.create(
                symbol = paired.symbol,
                earnings = createEarnings(paired.response),
                financials = createFinancials(paired.response),
                info = createInfo(paired.response),
            )
          }
        }
      }

  companion object {

    private data class YFEarnings(
        override val earningsDate: KeyStatistics.DataPoint,
        override val earningsAverage: KeyStatistics.DataPoint,
        override val earningsHigh: KeyStatistics.DataPoint,
        override val earningsLow: KeyStatistics.DataPoint,
        override val revenueAverage: KeyStatistics.DataPoint,
        override val revenueHigh: KeyStatistics.DataPoint,
        override val revenueLow: KeyStatistics.DataPoint,
    ) : KeyStatistics.Earnings

    @CheckResult
    private fun NetworkKeyStatisticsResponse.getData():
        NetworkKeyStatisticsResponse.Summary.Statistics {
      return this.quoteSummary.result.first()
    }

    @CheckResult
    private fun String?.asRecommendation(): KeyStatistics.Financials.Recommendation {
      return if (this == null) {
        Timber.w("Missing recommendation string")
        KeyStatistics.Financials.Recommendation.UNKNOWN
      } else {
        try {
          KeyStatistics.Financials.Recommendation.valueOf(this.uppercase())
        } catch (e: Throwable) {
          Timber.e(e, "Unknown recommendation string: $this")
          KeyStatistics.Financials.Recommendation.UNKNOWN
        }
      }
    }

    @JvmStatic
    @CheckResult
    private fun createEarnings(response: NetworkKeyStatisticsResponse): KeyStatistics.Earnings {
      val earnings = response.getData().calendarEvents.earnings
      return YFEarnings(
          earningsDate = earnings?.earningsDate?.firstOrNull().asDataPoint(),
          earningsAverage = earnings?.earningsAverage.asDataPoint(),
          earningsLow = earnings?.earningsLow.asDataPoint(),
          earningsHigh = earnings?.earningsHigh.asDataPoint(),
          revenueAverage = earnings?.revenueAverage.asDataPoint(),
          revenueLow = earnings?.revenueLow.asDataPoint(),
          revenueHigh = earnings?.revenueHigh.asDataPoint(),
      )
    }

    private data class YFFinancials(
        override val targetHighPrice: KeyStatistics.DataPoint,
        override val targetLowPrice: KeyStatistics.DataPoint,
        override val targetMeanPrice: KeyStatistics.DataPoint,
        override val recommendationMean: KeyStatistics.DataPoint,
        override val numberOfAnalystOpinions: KeyStatistics.DataPoint,
        override val recommendationKey: KeyStatistics.Financials.Recommendation
    ) : KeyStatistics.Financials

    @JvmStatic
    @CheckResult
    private fun createFinancials(response: NetworkKeyStatisticsResponse): KeyStatistics.Financials {
      val data = response.getData().financialData
      return YFFinancials(
          targetHighPrice = data.targetHighPrice.asDataPoint(),
          targetLowPrice = data.targetLowPrice.asDataPoint(),
          targetMeanPrice = data.targetMeanPrice.asDataPoint(),
          recommendationMean = data.recommendationMean.asDataPoint(),
          recommendationKey = data.recommendationKey.asRecommendation(),
          numberOfAnalystOpinions = data.numberOfAnalystOpinions.asDataPoint(),
      )
    }

    private data class YFInfo(
        override val beta: KeyStatistics.DataPoint,
        override val enterpriseValue: KeyStatistics.DataPoint,
        override val profitMargin: KeyStatistics.DataPoint,
        override val floatShares: KeyStatistics.DataPoint,
        override val sharesOutstanding: KeyStatistics.DataPoint,
        override val sharesShort: KeyStatistics.DataPoint,
        override val shortRatio: KeyStatistics.DataPoint,
        override val heldPercentInsiders: KeyStatistics.DataPoint,
        override val heldPercentInstitutions: KeyStatistics.DataPoint,
        override val shortPercentOfFloat: KeyStatistics.DataPoint,
        override val impliedSharesOutstanding: KeyStatistics.DataPoint,
        override val lastFiscalYearEnd: KeyStatistics.DataPoint,
        override val nextFiscalYearEnd: KeyStatistics.DataPoint,
        override val mostRecentQuarter: KeyStatistics.DataPoint,
        override val earningsQuarterlyGrowth: KeyStatistics.DataPoint,
        override val netIncomeToCommon: KeyStatistics.DataPoint,
        override val lastSplitDate: KeyStatistics.DataPoint,
        override val lastDividendValue: KeyStatistics.DataPoint,
        override val lastDividendDate: KeyStatistics.DataPoint,
        override val forwardEps: KeyStatistics.DataPoint,
        override val trailingEps: KeyStatistics.DataPoint
    ) : KeyStatistics.Info

    @JvmStatic
    @CheckResult
    private fun createInfo(response: NetworkKeyStatisticsResponse): KeyStatistics.Info {
      val data = response.getData().defaultKeyStatistics
      return YFInfo(
          beta = data.beta.asDataPoint(),
          enterpriseValue = data.enterpriseValue.asDataPoint(),
          profitMargin = data.profitMargin.asDataPoint(),
          floatShares = data.floatShares.asDataPoint(),
          sharesOutstanding = data.sharesOutstanding.asDataPoint(),
          sharesShort = data.sharesShort.asDataPoint(),
          shortRatio = data.shortRatio.asDataPoint(),
          heldPercentInsiders = data.heldPercentInsiders.asDataPoint(),
          heldPercentInstitutions = data.heldPercentInstitutions.asDataPoint(),
          shortPercentOfFloat = data.shortPercentOfFloat.asDataPoint(),
          impliedSharesOutstanding = data.impliedSharesOutstanding.asDataPoint(),
          lastFiscalYearEnd = data.lastFiscalYearEnd.asDataPoint(),
          nextFiscalYearEnd = data.nextFiscalYearEnd.asDataPoint(),
          mostRecentQuarter = data.mostRecentQuarter.asDataPoint(),
          earningsQuarterlyGrowth = data.earningsQuarterlyGrowth.asDataPoint(),
          netIncomeToCommon = data.netIncomeToCommon.asDataPoint(),
          lastSplitDate = data.lastSplitDate.asDataPoint(),
          lastDividendDate = data.lastDividendDate.asDataPoint(),
          lastDividendValue = data.lastDividendValue.asDataPoint(),
          forwardEps = data.forwardEps.asDataPoint(),
          trailingEps = data.trailingEps.asDataPoint(),
      )
    }

    private val ALL_MODULES_STRING = YFModules.values().joinToString(separator = ",") { it.module }

    private data class PairedResponse(
        val symbol: StockSymbol,
        val response: NetworkKeyStatisticsResponse,
    )

    private enum class YFModules(val module: String) {
      FINANCIAL_DATA("financialData"),
      KEY_STATISTICS("defaultKeyStatistics"),
      CALENDAR_EVENTS("calendarEvents"),
      INCOME_HISTORY("incomeStatementHistory"),
      CASHFLOW_HISTORY("cashflowStatementHistory"),
      BALANCE_SHEET_HISTORY("balanceSheetHistory"),
    }
  }
}
