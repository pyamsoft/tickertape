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

package com.pyamsoft.tickertape.watchlist.dig

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent
internal interface WatchlistDigComponent {

  // Name arg0 because otherwise DaggerTickerComponent is bugged dagger-2.43
  fun inject(arg0: WatchlistDigFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance symbol: StockSymbol,
        @BindsInstance @Named("lookup") lookupSymbol: StockSymbol,
        @BindsInstance allowAddToWatchlist: Boolean,
        @BindsInstance equityType: EquityType,
    ): WatchlistDigComponent
  }
}
