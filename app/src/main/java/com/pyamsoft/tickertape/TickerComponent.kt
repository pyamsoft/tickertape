/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.annotation.CheckResult
import coil.ImageLoader
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.alert.AlertAppModule
import com.pyamsoft.tickertape.db.DbModule
import com.pyamsoft.tickertape.db.room.RoomModule
import com.pyamsoft.tickertape.home.HomeModule
import com.pyamsoft.tickertape.main.MainActivity
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.portfolio.PortfolioAppModule
import com.pyamsoft.tickertape.portfolio.PortfolioRemoveInjector
import com.pyamsoft.tickertape.portfolio.dig.position.PositionComponent
import com.pyamsoft.tickertape.portfolio.dig.split.SplitComponent
import com.pyamsoft.tickertape.preference.PreferencesImpl
import com.pyamsoft.tickertape.quote.UiAppModule
import com.pyamsoft.tickertape.quote.add.NewTickerComponent
import com.pyamsoft.tickertape.stocks.StockModule
import com.pyamsoft.tickertape.stocks.remote.StockRemoteModule
import com.pyamsoft.tickertape.worker.work.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.worker.workmanager.WorkManagerAppModule
import com.pyamsoft.tickertape.worker.workmanager.WorkerComponent
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import java.time.Clock
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules =
        [
            TickerComponent.TickerProvider::class,

            // Stock network
            StockModule::class,
            UiAppModule::class,
            StockRemoteModule::class,

            // Screens
            HomeModule::class,
            PortfolioAppModule::class,

            // Database
            DbModule::class,
            RoomModule::class,

            // Workers
            WorkManagerAppModule::class,

            // Alerts
            AlertAppModule::class,
        ],
)
internal interface TickerComponent {

  fun inject(application: TickerTape)

  fun inject(injector: PortfolioRemoveInjector)

  @CheckResult fun plusWorkerComponent(): WorkerComponent.Factory

  @CheckResult fun plusMainComponent(): MainComponent.Factory

  @CheckResult fun plusPositionComponent(): PositionComponent.Factory

  @CheckResult fun plusSplitComponent(): SplitComponent.Factory

  @CheckResult fun plusNewTickerComponent(): NewTickerComponent.Factory

  @Component.Factory
  interface Factory {

    @CheckResult
    fun create(
        @Named("debug") @BindsInstance debug: Boolean,
        @BindsInstance application: Application,
        @BindsInstance imageLoader: ImageLoader,
        @BindsInstance theming: Theming,
        @BindsInstance enforcer: ThreadEnforcer,
    ): TickerComponent
  }

  @Module
  abstract class TickerProvider {

    @Binds
    @CheckResult
    abstract fun bindBigMoverPreferences(impl: PreferencesImpl): BigMoverPreferences

    @Module
    companion object {

      @Provides
      @JvmStatic
      internal fun provideActivityClass(): Class<out Activity> {
        return MainActivity::class.java
      }

      @Provides
      @JvmStatic
      internal fun provideContext(application: Application): Context {
        return application
      }

      @Provides
      @JvmStatic
      @Named("app_name")
      internal fun provideAppNameRes(): Int {
        return R.string.app_name
      }

      @Provides
      @JvmStatic
      @CheckResult
      internal fun provideClock(): Clock {
        return Clock.systemDefaultZone()
      }
    }
  }
}
