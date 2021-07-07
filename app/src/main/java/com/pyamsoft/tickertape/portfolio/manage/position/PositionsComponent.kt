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

package com.pyamsoft.tickertape.portfolio.manage.position

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModel
import com.pyamsoft.tickertape.core.ViewModelFactoryModule
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsViewModel
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(modules = [PositionsComponent.ComponentModule::class, ViewModelFactoryModule::class])
internal interface PositionsComponent {

  fun inject(fragment: PositionsFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(@BindsInstance parent: ViewGroup): PositionsComponent
  }

  @Module
  abstract class ComponentModule {

    @Binds
    @IntoMap
    @ClassKey(PositionsViewModel::class)
    internal abstract fun bindViewModel(impl: PositionsViewModel): ViewModel
  }
}
