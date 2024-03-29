/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.main

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.bus.internal.DefaultEventBus
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.home.HomeComponent
import com.pyamsoft.tickertape.notification.NotificationComponent
import com.pyamsoft.tickertape.portfolio.PortfolioComponent
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigComponent
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [MainComponent.MainModule::class])
internal interface MainComponent {

  @CheckResult fun plusHome(): HomeComponent.Factory

  @CheckResult fun plusPortfolio(): PortfolioComponent.Factory

  @CheckResult fun plusPortfolioDig(): PortfolioDigComponent.Factory

  @CheckResult fun plusAlerts(): NotificationComponent.Factory

  fun inject(activity: MainActivity)

  fun inject(injector: MainInjector)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(@BindsInstance activity: MainActivity): MainComponent
  }

  @Module
  abstract class MainModule {

    @Binds
    @CheckResult
    internal abstract fun bindMainSelectionEventConsumer(
        bus: EventBus<MainSelectionEvent>
    ): EventConsumer<MainSelectionEvent>

    @Module
    companion object {

      @Provides
      @JvmStatic
      @CheckResult
      @ActivityScope
      internal fun provideMainSelectionEventBus(): EventBus<MainSelectionEvent> {
        return DefaultEventBus()
      }
    }
  }
}
