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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.stocks.service.ChartService
import com.pyamsoft.tickertape.stocks.service.QuoteService
import com.pyamsoft.tickertape.stocks.service.TopService
import com.pyamsoft.tickertape.stocks.sources.ChartSource
import com.pyamsoft.tickertape.stocks.sources.QuoteSource
import com.pyamsoft.tickertape.stocks.sources.TopSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooChartSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooFinanceApi
import com.pyamsoft.tickertape.stocks.sources.yf.YahooFinanceSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooQuoteSource
import com.pyamsoft.tickertape.stocks.sources.yf.YahooTopSource
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Qualifier
import javax.net.SocketFactory
import kotlin.reflect.KClass
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val STOCK_BASE_URL = "https://finance.yahoo.com"
internal const val STOCK_API_URL = "${STOCK_BASE_URL}/"

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class InternalApi

private class OkHttpClientLazyCallFactory(debug: Boolean) : Call.Factory {

  private val client by lazy { createOkHttpClient(debug, DelegatingSocketFactory.create()) }

  override fun newCall(request: Request): Call {
    Enforcer.assertOffMainThread()
    return client.newCall(request)
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun createOkHttpClient(
        debug: Boolean,
        socketFactory: SocketFactory,
    ): OkHttpClient {
      Enforcer.assertOffMainThread()

      return OkHttpClient.Builder()
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

  // The actual quote source is YF
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindQuoteSource(impl: YahooFinanceSource): QuoteSource

  // The actual chart source is YF
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindChartSource(impl: YahooFinanceSource): ChartSource

  // The actual chart source is YF
  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindTopSource(impl: YahooFinanceSource): TopSource

  // The YFSource uses an internal YF quote source
  @Binds
  @CheckResult
  @YahooFinanceApi
  internal abstract fun bindYFQuoteSource(impl: YahooQuoteSource): QuoteSource

  // The YFSource uses an internal YF chart source
  @Binds
  @CheckResult
  @YahooFinanceApi
  internal abstract fun bindYFChartSource(impl: YahooChartSource): ChartSource

  // The YFSource uses an internal YF chart source
  @Binds
  @CheckResult
  @YahooFinanceApi
  internal abstract fun bindYFTopSource(impl: YahooTopSource): TopSource

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindNetworkInteractor(impl: StockNetworkInteractor): StockInteractor

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
    private fun createRetrofit(debug: Boolean, moshi: Moshi): Retrofit {
      return Retrofit.Builder()
          .baseUrl(STOCK_API_URL)
          .callFactory(OkHttpClientLazyCallFactory(debug))
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
    }

    @Provides
    @JvmStatic
    @InternalApi
    @CheckResult
    internal fun provideNetworkCreator(
        @Named("debug") debug: Boolean,
    ): NetworkServiceCreator {
      // Don't inject these to avoid needing Dagger API in build.gradle
      val retrofit = createRetrofit(debug, createMoshi())
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
  }
}
