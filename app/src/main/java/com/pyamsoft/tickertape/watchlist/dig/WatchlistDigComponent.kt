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

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistryOwner
import com.pyamsoft.tickertape.core.ViewModelFactoryModule
import com.pyamsoft.tickertape.quote.ui.chart.QuoteChartModule
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.ThemeProviderModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(
    modules =
        [
            WatchlistDigComponent.ComponentModule::class,
            ViewModelFactoryModule::class,
            ThemeProviderModule::class,
            QuoteChartModule::class,
        ])
internal interface WatchlistDigComponent {

  fun inject(dialog: WatchlistDigDialog)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance owner: SavedStateRegistryOwner,
        @BindsInstance lifecycleOwner: LifecycleOwner,
        @BindsInstance viewModelStore: ViewModelStore,
        @BindsInstance symbol: StockSymbol,
        @BindsInstance parent: ViewGroup,
    ): WatchlistDigComponent
  }

  @Module
  abstract class ComponentModule {

    @Binds
    @IntoMap
    @ClassKey(WatchlistDigViewModel::class)
    internal abstract fun bindViewModel(impl: WatchlistDigViewModel): ViewModel

  }
}