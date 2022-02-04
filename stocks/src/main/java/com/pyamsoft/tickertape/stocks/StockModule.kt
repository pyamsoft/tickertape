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
import com.pyamsoft.tickertape.stocks.cache.KeyStatisticsCache
import com.pyamsoft.tickertape.stocks.cache.OptionsCache
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.impl.MemoryKeyStatisticsCacheImpl
import com.pyamsoft.tickertape.stocks.cache.impl.MemoryOptionsCacheImpl
import com.pyamsoft.tickertape.stocks.cache.impl.MemoryStockCacheImpl
import com.pyamsoft.tickertape.stocks.okhttp.OkHttpClientLazyCallFactory
import com.pyamsoft.tickertape.stocks.scope.InternalStockApi
import com.pyamsoft.tickertape.stocks.scope.StockApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Qualifier
import kotlin.reflect.KClass
import okhttp3.Call
import retrofit2.Converter
import retrofit2.Retrofit

@Qualifier @Retention(AnnotationRetention.BINARY) private annotation class PrivateApi

@Module
abstract class StockModule {

  @Binds
  @CheckResult
  @InternalStockApi
  internal abstract fun bindNetworkInteractor(impl: StockNetworkInteractor): StockInteractor

  @Binds
  @CheckResult
  @InternalStockApi
  internal abstract fun bindStockCache(impl: MemoryStockCacheImpl): StockCache

  @Binds
  @CheckResult
  @InternalStockApi
  internal abstract fun bindKeyStatisticsCache(
      impl: MemoryKeyStatisticsCacheImpl
  ): KeyStatisticsCache

  @Binds
  @CheckResult
  @InternalStockApi
  internal abstract fun bindOptionsCache(impl: MemoryOptionsCacheImpl): OptionsCache

  @Binds
  @CheckResult
  internal abstract fun bindRealInteractor(impl: StockInteractorImpl): StockInteractor

  @Module
  companion object {

    /**
     * If this is @Provides, you will need to change okhttp3 from implementation to api in Gradle
     */
    @Provides
    @JvmStatic
    @CheckResult
    @StockApi
    internal fun createCallFactory(@Named("debug") debug: Boolean): Call.Factory {
      return OkHttpClientLazyCallFactory(debug)
    }

    /**
     * If this is @Provides, you will need to change retrofit from implementation to api in Gradle
     */
    @JvmStatic
    @CheckResult
    private fun createRetrofit(
        callFactory: Call.Factory,
        converterFactories: List<Converter.Factory>,
    ): Retrofit {
      return Retrofit.Builder()
          .baseUrl("https://your-service-should-be-replacing-this-url")
          .callFactory(callFactory)
          .run {
            var self = this
            for (factory in converterFactories) {
              self = self.addConverterFactory(factory)
            }
            return@run self
          }
          .build()
    }

    @Provides
    @JvmStatic
    @CheckResult
    @Named("json")
    internal fun provideJsonNetworkCreator(
        @StockApi callFactory: Call.Factory,
        @Named("moshi_converter") moshiConverter: Converter.Factory,
    ): NetworkServiceCreator {
      val retrofit =
          createRetrofit(
              callFactory = callFactory,
              converterFactories = listOf(moshiConverter),
          )
      return object : NetworkServiceCreator {
        override fun <T : Any> create(target: KClass<T>): T {
          return retrofit.create(target.java)
        }
      }
    }

    @Provides
    @JvmStatic
    @CheckResult
    @Named("xml")
    internal fun provideXmlNetworkCreator(
        @StockApi callFactory: Call.Factory,
        @Named("xml_converter") xmlConverter: Converter.Factory,
    ): NetworkServiceCreator {
      val retrofit =
          createRetrofit(
              callFactory = callFactory,
              converterFactories = listOf(xmlConverter),
          )
      return object : NetworkServiceCreator {
        override fun <T : Any> create(target: KClass<T>): T {
          return retrofit.create(target.java)
        }
      }
    }
  }
}
