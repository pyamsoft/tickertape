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
import com.pyamsoft.tickertape.stocks.cache.StockCache
import com.pyamsoft.tickertape.stocks.cache.impl.MemoryKeyStatisticsCacheImpl
import com.pyamsoft.tickertape.stocks.cache.impl.MemoryStockCacheImpl
import com.pyamsoft.tickertape.stocks.scope.InternalApi
import dagger.Binds
import dagger.Module

@Module
abstract class StockModule {

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindStockCache(impl: MemoryStockCacheImpl): StockCache

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindKeyStatisticsCache(
      impl: MemoryKeyStatisticsCacheImpl
  ): KeyStatisticsCache

  @Binds
  @CheckResult
  internal abstract fun bindRealInteractor(impl: StockInteractorImpl): StockInteractor
}
