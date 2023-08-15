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

package com.pyamsoft.tickertape.stocks.remote.source

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.stocks.api.KeyStatistics
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.network.NetworkKeyStatisticsResponse
import com.pyamsoft.tickertape.stocks.remote.service.KeyStatisticsService
import com.pyamsoft.tickertape.stocks.remote.storage.CookieProvider
import com.pyamsoft.tickertape.stocks.remote.yahoo.YahooCrumb
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class YahooKeyStatisticsSource
@Inject
internal constructor(
    @YahooApi private val service: KeyStatisticsService,
    @YahooApi private val cookie: CookieProvider<YahooCrumb>,
) : KeyStatisticSource {

  @CheckResult
  private suspend fun fetchKeyStatistics(symbol: StockSymbol): PairedResponse {
    val response =
        cookie.withAuth { auth ->
          service.getStatistics(
              cookie = auth.cookie,
              symbol = symbol.raw,
              crumb = auth.crumb,
              modules = ALL_MODULES_STRING,
          )
        }
    return PairedResponse(
        symbol = symbol,
        response = response,
    )
  }

  override suspend fun getKeyStatistics(symbols: List<StockSymbol>): List<KeyStatistics> =
      withContext(context = Dispatchers.Default) {
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
                // Quote is NULL here but may be paired in the future at the Interactor level
                quote = null,
                symbol = paired.symbol,
                earnings = createEarnings(paired.response),
                financials = createFinancials(paired.response),
                info = createInfo(paired.response),
            )
          }
        }
      }

  companion object {

    private val EMPTY_DOUBLE =
        object : KeyStatistics.DataPoint<Double> {

          override val raw: Double = 0.0

          override val fmt: String? = null

          override val isEmpty: Boolean = true
        }

    private val EMPTY_LONG =
        object : KeyStatistics.DataPoint<Long> {

          override val raw: Long = 0

          override val fmt: String? = null

          override val isEmpty: Boolean = true
        }

    private data class YFDataPoint<T : Number>(
        override val raw: T,
        override val fmt: String?,
    ) : KeyStatistics.DataPoint<T> {
      override val isEmpty: Boolean = false
    }

    @CheckResult
    internal fun NetworkKeyStatisticsResponse.YFData?.asLongDataPoint(
        long: Boolean = false
    ): KeyStatistics.DataPoint<Long> {
      if (this == null || this.raw == null) {
        return EMPTY_LONG
      }

      val f = if (long) this.longFmt ?: this.fmt else this.fmt
      return when (this.raw) {
        // Straight Long
        is Long -> {
          YFDataPoint(
              raw = this.raw,
              fmt = f,
          )
        }
        // Double long like 1.6667E9
        is Double -> {
          YFDataPoint(
              raw = this.raw.toLong(),
              fmt = f,
          )
        }
        else -> {
          Timber.w(
              "Invalid raw value for DataPoint<Long>: ${this.raw} ${this.raw::class.java.name}")
          EMPTY_LONG
        }
      }
    }

    @CheckResult
    internal fun NetworkKeyStatisticsResponse.YFData?.asDoubleDataPoint(
        long: Boolean = false
    ): KeyStatistics.DataPoint<Double> {
      if (this == null || this.raw == null) {
        return EMPTY_DOUBLE
      }

      val f = if (long) this.longFmt ?: this.fmt else this.fmt
      return if (this.raw is Number) {
        YFDataPoint(
            raw = this.raw.toDouble(),
            fmt = f,
        )
      } else if (this.raw is String && this.raw == "Infinity") {
        // Infinity is special
        YFDataPoint(
            raw = Double.POSITIVE_INFINITY,
            fmt = f,
        )
      } else {
        Timber.w("Invalid raw value for DataPoint<Double>: ${this.raw}")
        EMPTY_DOUBLE
      }
    }

    private data class YFEarnings(
        override val earningsDate: KeyStatistics.DataPoint<Long>,
        override val earningsAverage: KeyStatistics.DataPoint<Double>,
        override val earningsHigh: KeyStatistics.DataPoint<Double>,
        override val earningsLow: KeyStatistics.DataPoint<Double>,
        override val revenueAverage: KeyStatistics.DataPoint<Long>,
        override val revenueHigh: KeyStatistics.DataPoint<Long>,
        override val revenueLow: KeyStatistics.DataPoint<Long>,
        override val exDividendDate: KeyStatistics.DataPoint<Long>,
        override val dividendDate: KeyStatistics.DataPoint<Long>,
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
          e.ifNotCancellation {
            Timber.w(e, "Unknown recommendation string: $this, fallback to UNKNOWN")
            KeyStatistics.Financials.Recommendation.UNKNOWN
          }
        }
      }
    }

    @JvmStatic
    @CheckResult
    private fun createEarnings(response: NetworkKeyStatisticsResponse): KeyStatistics.Earnings? {
      val calendarEvents = response.getData().calendarEvents
      if (calendarEvents == null) {
        return null
      } else {
        val earnings = calendarEvents.earnings
        return if (earnings == null) null
        else {
          YFEarnings(
              earningsDate = earnings.earningsDate?.firstOrNull().asLongDataPoint(),
              earningsAverage = earnings.earningsAverage.asDoubleDataPoint(),
              earningsLow = earnings.earningsLow.asDoubleDataPoint(),
              earningsHigh = earnings.earningsHigh.asDoubleDataPoint(),
              revenueAverage = earnings.revenueAverage.asLongDataPoint(),
              revenueLow = earnings.revenueLow.asLongDataPoint(),
              revenueHigh = earnings.revenueHigh.asLongDataPoint(),
              exDividendDate = calendarEvents.exDividendDate.asLongDataPoint(),
              dividendDate = calendarEvents.dividendDate.asLongDataPoint(),
          )
        }
      }
    }

    private data class YFFinancials(
        override val currentPrice: KeyStatistics.DataPoint<Double>,
        override val targetHighPrice: KeyStatistics.DataPoint<Double>,
        override val targetLowPrice: KeyStatistics.DataPoint<Double>,
        override val targetMeanPrice: KeyStatistics.DataPoint<Double>,
        override val recommendationMean: KeyStatistics.DataPoint<Double>,
        override val numberOfAnalystOpinions: KeyStatistics.DataPoint<Long>,
        override val recommendationKey: KeyStatistics.Financials.Recommendation,
        override val returnOnAssets: KeyStatistics.DataPoint<Double>,
        override val returnOnEquity: KeyStatistics.DataPoint<Double>,
        override val profitMargin: KeyStatistics.DataPoint<Double>,
        override val operatingMargin: KeyStatistics.DataPoint<Double>,
        override val ebitdaMargin: KeyStatistics.DataPoint<Double>,
        override val grossMargin: KeyStatistics.DataPoint<Double>,
        override val revenuePerShare: KeyStatistics.DataPoint<Double>,
        override val totalRevenue: KeyStatistics.DataPoint<Long>,
        override val revenueGrowth: KeyStatistics.DataPoint<Double>,
        override val grossProfits: KeyStatistics.DataPoint<Long>,
        override val freeCashflow: KeyStatistics.DataPoint<Long>,
        override val operatingCashflow: KeyStatistics.DataPoint<Long>,
        override val currentRatio: KeyStatistics.DataPoint<Double>,
        override val ebitda: KeyStatistics.DataPoint<Long>,
        override val totalDebt: KeyStatistics.DataPoint<Long>,
        override val totalCashPerShare: KeyStatistics.DataPoint<Double>,
        override val quickRatio: KeyStatistics.DataPoint<Double>,
        override val debtToEquity: KeyStatistics.DataPoint<Double>,
        override val totalCash: KeyStatistics.DataPoint<Long>,
        override val earningsGrowth: KeyStatistics.DataPoint<Double>,
    ) : KeyStatistics.Financials

    @JvmStatic
    @CheckResult
    private fun createFinancials(
        response: NetworkKeyStatisticsResponse
    ): KeyStatistics.Financials? {
      val data = response.getData().financialData
      return if (data == null) null
      else {
        YFFinancials(
            currentPrice = data.currentPrice.asDoubleDataPoint(),
            targetHighPrice = data.targetHighPrice.asDoubleDataPoint(),
            targetLowPrice = data.targetLowPrice.asDoubleDataPoint(),
            targetMeanPrice = data.targetMeanPrice.asDoubleDataPoint(),
            recommendationMean = data.recommendationMean.asDoubleDataPoint(),
            recommendationKey = data.recommendationKey.asRecommendation(),
            numberOfAnalystOpinions = data.numberOfAnalystOpinions.asLongDataPoint(),
            profitMargin = data.profitMargins.asDoubleDataPoint(),
            ebitdaMargin = data.ebitdaMargins.asDoubleDataPoint(),
            operatingMargin = data.operatingMargins.asDoubleDataPoint(),
            grossMargin = data.grossMargins.asDoubleDataPoint(),
            returnOnAssets = data.returnOnAssets.asDoubleDataPoint(),
            returnOnEquity = data.returnOnEquity.asDoubleDataPoint(),
            totalRevenue = data.totalRevenue.asLongDataPoint(),
            revenuePerShare = data.revenuePerShare.asDoubleDataPoint(),
            revenueGrowth = data.revenueGrowth.asDoubleDataPoint(),
            grossProfits = data.grossProfits.asLongDataPoint(),
            freeCashflow = data.freeCashflow.asLongDataPoint(),
            operatingCashflow = data.operatingCashflow.asLongDataPoint(),
            currentRatio = data.currentRatio.asDoubleDataPoint(),
            earningsGrowth = data.earningsGrowth.asDoubleDataPoint(),
            ebitda = data.ebitda.asLongDataPoint(),
            debtToEquity = data.debtToEquity.asDoubleDataPoint(),
            totalCashPerShare = data.totalCashPerShare.asDoubleDataPoint(),
            totalDebt = data.totalDebt.asLongDataPoint(),
            quickRatio = data.quickRatio.asDoubleDataPoint(),
            totalCash = data.totalCash.asLongDataPoint(),
        )
      }
    }

    private data class YFInfo(
        override val beta: KeyStatistics.DataPoint<Double>,
        override val enterpriseValue: KeyStatistics.DataPoint<Long>,
        override val floatShares: KeyStatistics.DataPoint<Long>,
        override val sharesOutstanding: KeyStatistics.DataPoint<Long>,
        override val sharesShort: KeyStatistics.DataPoint<Long>,
        override val shortRatio: KeyStatistics.DataPoint<Double>,
        override val heldPercentInsiders: KeyStatistics.DataPoint<Double>,
        override val heldPercentInstitutions: KeyStatistics.DataPoint<Double>,
        override val shortPercentOfFloat: KeyStatistics.DataPoint<Double>,
        override val lastFiscalYearEnd: KeyStatistics.DataPoint<Long>,
        override val nextFiscalYearEnd: KeyStatistics.DataPoint<Long>,
        override val mostRecentQuarter: KeyStatistics.DataPoint<Long>,
        override val netIncomeToCommon: KeyStatistics.DataPoint<Long>,
        override val lastSplitDate: KeyStatistics.DataPoint<Long>,
        override val lastDividendValue: KeyStatistics.DataPoint<Double>,
        override val lastDividendDate: KeyStatistics.DataPoint<Long>,
        override val forwardEps: KeyStatistics.DataPoint<Double>,
        override val trailingEps: KeyStatistics.DataPoint<Double>,
        override val forwardPE: KeyStatistics.DataPoint<Double>,
        override val pegRatio: KeyStatistics.DataPoint<Double>,
        override val enterpriseValueToEbitda: KeyStatistics.DataPoint<Double>,
        override val enterpriseValueToRevenue: KeyStatistics.DataPoint<Double>,
        override val priceToBook: KeyStatistics.DataPoint<Double>,
        override val bookValue: KeyStatistics.DataPoint<Double>,
        override val fiftyTwoWeekChange: KeyStatistics.DataPoint<Double>,
        override val marketFiftyTwoWeekChange: KeyStatistics.DataPoint<Double>,
        override val lastSplitFactor: String,
    ) : KeyStatistics.Info

    @JvmStatic
    @CheckResult
    private fun createInfo(response: NetworkKeyStatisticsResponse): KeyStatistics.Info? {
      val data = response.getData().defaultKeyStatistics
      return if (data == null) null
      else {
        YFInfo(
            beta = data.beta.asDoubleDataPoint(),
            enterpriseValue = data.enterpriseValue.asLongDataPoint(),
            floatShares = data.floatShares.asLongDataPoint(),
            sharesOutstanding = data.sharesOutstanding.asLongDataPoint(),
            sharesShort = data.sharesShort.asLongDataPoint(),
            shortRatio = data.shortRatio.asDoubleDataPoint(),
            heldPercentInsiders = data.heldPercentInsiders.asDoubleDataPoint(),
            heldPercentInstitutions = data.heldPercentInstitutions.asDoubleDataPoint(),
            shortPercentOfFloat = data.shortPercentOfFloat.asDoubleDataPoint(),
            lastFiscalYearEnd = data.lastFiscalYearEnd.asLongDataPoint(),
            nextFiscalYearEnd = data.nextFiscalYearEnd.asLongDataPoint(),
            mostRecentQuarter = data.mostRecentQuarter.asLongDataPoint(),
            netIncomeToCommon = data.netIncomeToCommon.asLongDataPoint(),
            lastSplitDate = data.lastSplitDate.asLongDataPoint(),
            lastDividendDate = data.lastDividendDate.asLongDataPoint(),
            lastDividendValue = data.lastDividendValue.asDoubleDataPoint(),
            forwardEps = data.forwardEps.asDoubleDataPoint(),
            trailingEps = data.trailingEps.asDoubleDataPoint(),
            pegRatio = data.pegRatio.asDoubleDataPoint(),
            bookValue = data.bookValue.asDoubleDataPoint(),
            priceToBook = data.priceToBook.asDoubleDataPoint(),
            enterpriseValueToEbitda = data.enterpriseToEbitda.asDoubleDataPoint(),
            enterpriseValueToRevenue = data.enterpriseToRevenue.asDoubleDataPoint(),
            forwardPE = data.forwardPE.asDoubleDataPoint(),
            fiftyTwoWeekChange = data.fiftyTwoWeekChange.asDoubleDataPoint(),
            marketFiftyTwoWeekChange = data.marketFiftyTwoWeekChange.asDoubleDataPoint(),
            lastSplitFactor = data.lastSplitFactor.orEmpty().trim(),
        )
      }
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
