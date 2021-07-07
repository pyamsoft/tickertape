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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistryOwner
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.watchlist.dig.range.WatchlistDigRangeComponent
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
internal interface BaseWatchlistDigComponent {

  @CheckResult fun plusDigComponent(): WatchlistDigComponent.Factory

  /** Not actually used, just here so graph can compile */
  @CheckResult
  @Suppress("FunctionName")
  fun `$$daggerRequiredWatchlistDigRangeComponent`(): WatchlistDigRangeComponent.Factory

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance owner: SavedStateRegistryOwner,
        @BindsInstance lifecycleOwner: LifecycleOwner,
        @BindsInstance viewModelStore: ViewModelStore,
        @BindsInstance symbol: StockSymbol,
    ): BaseWatchlistDigComponent
  }
}
