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

package com.pyamsoft.tickertape.home

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.tickertape.core.ViewModelFactoryModule
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
            HomeComponent.ComponentModule::class,
            ViewModelFactoryModule::class,
            ThemeProviderModule::class,
        ])
internal interface HomeComponent {

  fun inject(fragment: HomeFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance parent: ViewGroup,
    ): HomeComponent
  }

  @Module
  abstract class ComponentModule {

    @Binds
    @IntoMap
    @ClassKey(HomeViewModel::class)
    internal abstract fun bindViewModel(impl: HomeViewModel): ViewModel

    @Module
    companion object {

        @Provides
        @JvmStatic
        @CheckResult
        internal fun provideRecyclerPool(): RecyclerView.RecycledViewPool {
            return RecyclerView.RecycledViewPool()
        }
    }
  }
}
