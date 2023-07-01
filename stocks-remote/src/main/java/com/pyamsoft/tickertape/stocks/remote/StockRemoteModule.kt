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

package com.pyamsoft.tickertape.stocks.remote

import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.NetworkServiceCreator
import com.pyamsoft.tickertape.stocks.remote.api.RobinhoodApi
import com.pyamsoft.tickertape.stocks.remote.api.YahooApi
import com.pyamsoft.tickertape.stocks.remote.converter.QualifiedTypeConverterFactory
import com.pyamsoft.tickertape.stocks.remote.robinhood.RobinhoodCookieService
import com.pyamsoft.tickertape.stocks.remote.robinhood.RobinhoodCookieStorage
import com.pyamsoft.tickertape.stocks.remote.robinhood.RobinhoodToken
import com.pyamsoft.tickertape.stocks.remote.service.ChartService
import com.pyamsoft.tickertape.stocks.remote.service.KeyStatisticsService
import com.pyamsoft.tickertape.stocks.remote.service.NewsService
import com.pyamsoft.tickertape.stocks.remote.service.OptionsService
import com.pyamsoft.tickertape.stocks.remote.service.QuoteService
import com.pyamsoft.tickertape.stocks.remote.service.RecommendationService
import com.pyamsoft.tickertape.stocks.remote.service.SearchService
import com.pyamsoft.tickertape.stocks.remote.service.TopService
import com.pyamsoft.tickertape.stocks.remote.source.RobinhoodNewsSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooChartSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooKeyStatisticsSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooOptionsSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooQuoteSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooRecommendationSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooSearchSource
import com.pyamsoft.tickertape.stocks.remote.source.YahooTopSource
import com.pyamsoft.tickertape.stocks.remote.storage.CookieProvider
import com.pyamsoft.tickertape.stocks.remote.yahoo.YahooCookieService
import com.pyamsoft.tickertape.stocks.remote.yahoo.YahooCookieStorage
import com.pyamsoft.tickertape.stocks.remote.yahoo.YahooCrumb
import com.pyamsoft.tickertape.stocks.scope.StockApi
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.KeyStatisticSource
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.RecommendationSource
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.sources.TopSource
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Named
import javax.inject.Singleton
import retrofit2.Converter
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Module
abstract class StockRemoteModule {

  @Binds @StockApi internal abstract fun bindQuoteSource(impl: YahooQuoteSource): QuoteSource

  @Binds @StockApi internal abstract fun bindOptionsSource(impl: YahooOptionsSource): OptionsSource

  @Binds @StockApi internal abstract fun bindChartSource(impl: YahooChartSource): ChartSource

  @Binds
  @StockApi
  internal abstract fun bindKeyStatisticsSource(impl: YahooKeyStatisticsSource): KeyStatisticSource

  @Binds @StockApi internal abstract fun bindSearchSource(impl: YahooSearchSource): SearchSource

  @Binds @StockApi internal abstract fun bindTopSource(impl: YahooTopSource): TopSource

  @Binds
  @StockApi
  internal abstract fun bindRecommendationsSource(
      impl: YahooRecommendationSource
  ): RecommendationSource

  @Binds @StockApi internal abstract fun bindNewsSource(impl: RobinhoodNewsSource): NewsSource

  @Binds
  @YahooApi
  internal abstract fun bindYahooCrumbProvider(impl: YahooCookieStorage): CookieProvider<YahooCrumb>

  @Binds
  @RobinhoodApi
  internal abstract fun bindRobinhoodProvider(
      impl: RobinhoodCookieStorage
  ): CookieProvider<RobinhoodToken>

  /** Expose globally */
  @Binds internal abstract fun bindJsonParser(impl: MoshiJsonParser): JsonParser

  @Module
  companion object {

    @Provides
    @StockApi
    @JvmStatic
    @Singleton
    internal fun provideMoshi(): Moshi {
      return Moshi.Builder().build()
    }

    @StockApi
    @Provides
    @JvmStatic
    internal fun provideQualifiedConverterFactory(
        @Named("scalar") scalarConverter: Converter.Factory,
        // Need to use MutableSet instead of Set because of Java -> Kotlin fun.
        @StockApi converters: MutableSet<Converter.Factory>,
    ): Converter.Factory {
      return QualifiedTypeConverterFactory.create(
          scalar = scalarConverter,
          converters = converters,
      )
    }

    @Provides
    @JvmStatic
    @Named("scalar")
    internal fun provideYahooConverterFactory(): Converter.Factory {
      return ScalarsConverterFactory.create()
    }

    @Provides
    @JvmStatic
    @IntoSet
    @StockApi
    internal fun provideMoshiConverterFactory(@StockApi moshi: Moshi): Converter.Factory {
      return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @JvmStatic
    @RobinhoodApi
    internal fun provideNews(serviceCreator: NetworkServiceCreator): NewsService {
      return serviceCreator.create(NewsService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideQuotes(serviceCreator: NetworkServiceCreator): QuoteService {
      return serviceCreator.create(QuoteService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideCharts(serviceCreator: NetworkServiceCreator): ChartService {
      return serviceCreator.create(ChartService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideTops(serviceCreator: NetworkServiceCreator): TopService {
      return serviceCreator.create(TopService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideSearch(serviceCreator: NetworkServiceCreator): SearchService {
      return serviceCreator.create(SearchService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideOptions(serviceCreator: NetworkServiceCreator): OptionsService {
      return serviceCreator.create(OptionsService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideKeyStatistics(serviceCreator: NetworkServiceCreator): KeyStatisticsService {
      return serviceCreator.create(KeyStatisticsService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideRecommendations(
        serviceCreator: NetworkServiceCreator
    ): RecommendationService {
      return serviceCreator.create(RecommendationService::class)
    }

    @Provides
    @YahooApi
    @JvmStatic
    internal fun provideYahooCookies(serviceCreator: NetworkServiceCreator): YahooCookieService {
      return serviceCreator.create(YahooCookieService::class)
    }

    @Provides
    @JvmStatic
    @RobinhoodApi
    internal fun provideRobinhoodCookies(
        serviceCreator: NetworkServiceCreator
    ): RobinhoodCookieService {
      return serviceCreator.create(RobinhoodCookieService::class)
    }
  }
}
