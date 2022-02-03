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

package com.pyamsoft.tickertape.stocks.yahoo

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.NetworkServiceCreator
import com.pyamsoft.tickertape.stocks.scope.StockApi
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.sources.TopSource
import com.pyamsoft.tickertape.stocks.yahoo.service.ChartService
import com.pyamsoft.tickertape.stocks.yahoo.service.KeyStatisticsService
import com.pyamsoft.tickertape.stocks.yahoo.service.OptionsService
import com.pyamsoft.tickertape.stocks.yahoo.service.QuoteService
import com.pyamsoft.tickertape.stocks.yahoo.service.SearchService
import com.pyamsoft.tickertape.stocks.yahoo.service.TopService
import com.pyamsoft.tickertape.stocks.yahoo.source.YahooChartSource
import com.pyamsoft.tickertape.stocks.yahoo.source.YahooKeyStatisticsSource
import com.pyamsoft.tickertape.stocks.yahoo.source.YahooOptionsSource
import com.pyamsoft.tickertape.stocks.yahoo.source.YahooQuoteSource
import com.pyamsoft.tickertape.stocks.yahoo.source.YahooSearchSource
import com.pyamsoft.tickertape.stocks.yahoo.source.YahooTopSource
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class YahooFinanceModule {

  @Binds
  @StockApi
  @CheckResult
  internal abstract fun bindYFQuoteSource(impl: YahooQuoteSource): QuoteSource

  @Binds
  @StockApi
  @CheckResult
  internal abstract fun bindYFOptionsSource(impl: YahooOptionsSource): OptionsSource

  @Binds
  @StockApi
  @CheckResult
  internal abstract fun bindYFChartSource(impl: YahooChartSource): ChartSource

  @Binds
  @StockApi
  @CheckResult
  internal abstract fun bindYFTopSource(impl: YahooTopSource): TopSource

  @Binds
  @StockApi
  @CheckResult
  internal abstract fun bindYFKeyStatisticsSource(
      impl: YahooKeyStatisticsSource
  ): KeyStatisticSource

  @Binds
  @StockApi
  @CheckResult
  internal abstract fun bindYFSearchSource(impl: YahooSearchSource): SearchSource

  @Module
  companion object {

    @Provides
    @YahooApi
    @JvmStatic
    @CheckResult
    internal fun provideQuotes(@StockApi serviceCreator: NetworkServiceCreator): QuoteService {
      return serviceCreator.create(QuoteService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    @CheckResult
    internal fun provideCharts(@StockApi serviceCreator: NetworkServiceCreator): ChartService {
      return serviceCreator.create(ChartService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    @CheckResult
    internal fun provideTops(@StockApi serviceCreator: NetworkServiceCreator): TopService {
      return serviceCreator.create(TopService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    @CheckResult
    internal fun provideSearch(@StockApi serviceCreator: NetworkServiceCreator): SearchService {
      return serviceCreator.create(SearchService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    @CheckResult
    internal fun provideOptions(@StockApi serviceCreator: NetworkServiceCreator): OptionsService {
      return serviceCreator.create(OptionsService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    @CheckResult
    internal fun provideKeyStatistics(
        @StockApi serviceCreator: NetworkServiceCreator
    ): KeyStatisticsService {
      return serviceCreator.create(KeyStatisticsService::class)
    }
  }
}
