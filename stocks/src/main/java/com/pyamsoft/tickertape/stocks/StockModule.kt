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

package com.pyamsoft.tickertape.stocks

import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.cache.MemoryStockCacheImpl
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.service.ChartService
import com.pyamsoft.tickertape.stocks.service.OptionsService
import com.pyamsoft.tickertape.stocks.service.QuoteService
import com.pyamsoft.tickertape.stocks.service.SearchService
import com.pyamsoft.tickertape.stocks.service.TopService
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.OptionsSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.SearchSource
import com.pyamsoft.tickertape.stocks.sources.TopSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooChartSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooOptionsSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooQuoteSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooSearchSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooTopSource
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.net.SocketFactory
import kotlin.reflect.KClass
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val STOCK_BASE_URL = "https://finance.yahoo.com"
internal const val STOCK_API_URL = "${STOCK_BASE_URL}/"

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class InternalApi

private class OkHttpClientLazyCallFactory(context: Context, debug: Boolean) : Call.Factory {

  private val client by lazy {
    createOkHttpClient(context, debug, DelegatingSocketFactory.create())
  }

  override fun newCall(request: Request): Call {
    Enforcer.assertOffMainThread()
    return client.newCall(request)
  }

  /** Cache all requests for at least 15 seconds to avoid overloading third party endpoints. */
  private class AlwaysCachingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
      val cacheControl = CacheControl.Builder().maxAge(15, TimeUnit.SECONDS).build()

      val response = chain.proceed(chain.request())
      val cachingStrategy = response.header(CACHE_CONTROL_HEADER)
      return if (cachingStrategy != CACHE_CONTROL_NO_CACHING) response
      else {
        response.newBuilder().header(CACHE_CONTROL_HEADER, cacheControl.toString()).build()
      }
    }

    companion object {
      private const val CACHE_CONTROL_HEADER = "Cache-Control"
      private const val CACHE_CONTROL_NO_CACHING = "no-cache"
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun createOkHttpClient(
        context: Context,
        debug: Boolean,
        socketFactory: SocketFactory,
    ): OkHttpClient {
      Enforcer.assertOffMainThread()

      // Cache up to 1MB of data
      val diskCache = Cache(context.applicationContext.cacheDir, 1_000_000L)

      return OkHttpClient.Builder()
          .cache(diskCache)
          .addNetworkInterceptor(AlwaysCachingInterceptor())
          .socketFactory(socketFactory)
          .apply {
            if (debug) {
              addInterceptor(
                  HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            }
          }
          .build()
    }
  }
}

@Module
abstract class StockModule {

  // The YFSource uses an internal YF quote source
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindYFQuoteSource(impl: YahooQuoteSource): QuoteSource

  // The YFSource uses an internal YF options source
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindYFOptionsSource(impl: YahooOptionsSource): OptionsSource

  // The YFSource uses an internal YF chart source
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindYFChartSource(impl: YahooChartSource): ChartSource

  // The YFSource uses an internal YF top source
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindYFTopSource(impl: YahooTopSource): TopSource

  // The YFSource uses an internal YF search source
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindYFSearchSource(impl: YahooSearchSource): SearchSource

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindNetworkInteractor(impl: StockNetworkInteractor): StockInteractor

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindStockCache(impl: MemoryStockCacheImpl): StockCache

  @Binds
  @CheckResult
  internal abstract fun bindRealInteractor(impl: StockInteractorImpl): StockInteractor

  @Module
  companion object {

    @JvmStatic
    @CheckResult
    private fun createMoshi(): Moshi {
      return Moshi.Builder().build()
    }

    @JvmStatic
    @CheckResult
    private fun createRetrofit(context: Context, debug: Boolean, moshi: Moshi): Retrofit {
      return Retrofit.Builder()
          .baseUrl(STOCK_API_URL)
          .callFactory(OkHttpClientLazyCallFactory(context, debug))
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideNetworkCreator(
        context: Context,
        @Named("debug") debug: Boolean,
    ): NetworkServiceCreator {
      // Don't inject these to avoid needing Dagger API in build.gradle
      val retrofit = createRetrofit(context, debug, createMoshi())
      return object : NetworkServiceCreator {

        override fun <T : Any> create(target: KClass<T>): T {
          return retrofit.create(target.java)
        }
      }
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideQuotes(@InternalApi serviceCreator: NetworkServiceCreator): QuoteService {
      return serviceCreator.create(QuoteService::class)
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideCharts(@InternalApi serviceCreator: NetworkServiceCreator): ChartService {
      return serviceCreator.create(ChartService::class)
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideTops(@InternalApi serviceCreator: NetworkServiceCreator): TopService {
      return serviceCreator.create(TopService::class)
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideSearch(@InternalApi serviceCreator: NetworkServiceCreator): SearchService {
      return serviceCreator.create(SearchService::class)
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideOptions(
        @InternalApi serviceCreator: NetworkServiceCreator
    ): OptionsService {
      return serviceCreator.create(OptionsService::class)
    }
  }
}
