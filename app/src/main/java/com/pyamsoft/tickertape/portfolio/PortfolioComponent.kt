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

package com.pyamsoft.tickertape.portfolio

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.tickertape.core.ViewModelFactoryModule
import com.pyamsoft.tickertape.ui.ThemeProviderModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(
    modules =
        [
            PortfolioComponent.ComponentModule::class,
            ViewModelFactoryModule::class,
            ThemeProviderModule::class,
        ])
internal interface PortfolioComponent {

  fun inject(fragment: PortfolioFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance toolbarActivity: ToolbarActivity,
        @BindsInstance activity: Activity,
        @BindsInstance owner: LifecycleOwner,
        @BindsInstance parent: ViewGroup,
    ): PortfolioComponent
  }

  @Module
  abstract class ComponentModule {

    @Binds
    @IntoMap
    @ClassKey(PortfolioViewModel::class)
    internal abstract fun bindViewModel(impl: PortfolioViewModel): ViewModel
  }
}
