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

package com.pyamsoft.tickertape.stocks.googlenews

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.NetworkServiceCreator
import com.pyamsoft.tickertape.stocks.googlenews.service.NewsService
import com.pyamsoft.tickertape.stocks.googlenews.source.GoogleNewsSource
import com.pyamsoft.tickertape.stocks.scope.StockApi
import com.pyamsoft.tickertape.stocks.sources.NewsSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import retrofit2.Converter

@Module
abstract class GoogleNewsModule {

  @Module
  companion object {

    @Provides
    @JvmStatic
    @CheckResult
    @Named("xml_converter")
    internal fun provideXmlConverterFactory(): Converter.Factory {
      @Suppress("DEPRECATION")
      return retrofit2.converter.simplexml.SimpleXmlConverterFactory.createNonStrict()
    }

    @Provides
    @JvmStatic
    @CheckResult
    @GoogleNewsApi
    internal fun provideNews(@Named("xml") serviceCreator: NetworkServiceCreator): NewsService {
      return serviceCreator.create(NewsService::class)
    }
  }
}
