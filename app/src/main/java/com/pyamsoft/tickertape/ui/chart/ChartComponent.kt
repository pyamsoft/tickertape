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

package com.pyamsoft.tickertape.ui.chart

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModel
import com.pyamsoft.tickertape.core.ViewModelFactoryModule
import com.pyamsoft.tickertape.portfolio.manage.chart.PositionChartFragment
import com.pyamsoft.tickertape.quote.ui.component.chart.StockChartViewModel
import com.pyamsoft.tickertape.ui.ThemeProviderModule
import com.pyamsoft.tickertape.watchlist.dig.chart.WatchlistDigChartFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(
    modules =
        [
            ChartComponent.ComponentModule::class,
            ViewModelFactoryModule::class,
            ThemeProviderModule::class,
        ])
internal interface ChartComponent {

  fun inject(dialog: WatchlistDigChartFragment)

  fun inject(dialog: PositionChartFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(@BindsInstance parent: ViewGroup): ChartComponent
  }

  @Module
  abstract class ComponentModule {

    @Binds
    @IntoMap
    @ClassKey(StockChartViewModel::class)
    internal abstract fun bindViewModel(impl: StockChartViewModel): ViewModel
  }
}
