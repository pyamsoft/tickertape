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

package com.pyamsoft.tickertape

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.alert.AlertModule
import com.pyamsoft.tickertape.alert.inject.AlertComponent
import com.pyamsoft.tickertape.alert.preference.BigMoverPreferences
import com.pyamsoft.tickertape.alert.workmanager.WorkManagerModule
import com.pyamsoft.tickertape.db.DbModule
import com.pyamsoft.tickertape.db.room.RoomModule
import com.pyamsoft.tickertape.home.HomeModule
import com.pyamsoft.tickertape.main.MainActivity
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.portfolio.PortfolioModule
import com.pyamsoft.tickertape.portfolio.add.BasePortfolioAddComponent
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigComponent
import com.pyamsoft.tickertape.preference.PreferencesImpl
import com.pyamsoft.tickertape.quote.TickerModule
import com.pyamsoft.tickertape.receiver.BootReceiver
import com.pyamsoft.tickertape.receiver.ScreenReceiver
import com.pyamsoft.tickertape.stocks.StockModule
import com.pyamsoft.tickertape.tape.TapeComponent
import com.pyamsoft.tickertape.tape.TapeModule
import com.pyamsoft.tickertape.tape.TapePreferences
import com.pyamsoft.tickertape.tape.TapeService
import com.pyamsoft.tickertape.watchlist.WatchlistModule
import com.pyamsoft.tickertape.watchlist.add.BaseWatchlistAddComponent
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigComponent
import dagger.*
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules =
        [
            TickerComponent.TickerProvider::class,
            StockModule::class,
            DbModule::class,
            RoomModule::class,
            TapeModule::class,
            AlertModule::class,
            WorkManagerModule::class,
            TickerModule::class,
            HomeModule::class,
            WatchlistModule::class,
            PortfolioModule::class,
        ],
)
internal interface TickerComponent {

  // ===============================================
  // HACKY INJECTORS

  /* FROM inside BigMoverInjector, RefresherInjector: See TickerTape Injector */
  @CheckResult fun plusAlertComponent(): AlertComponent

  // ===============================================

  fun inject(receiver: BootReceiver)

  fun inject(receiver: ScreenReceiver)

  fun inject(application: TickerTape)

  @CheckResult fun plusTapeComponent(): TapeComponent.Factory

  @CheckResult fun plusWatchlistAddComponent(): BaseWatchlistAddComponent.Factory

  @CheckResult fun plusPortfolioAddComponent(): BasePortfolioAddComponent.Factory

  @CheckResult fun plusMainComponent(): MainComponent.Factory

  @CheckResult fun plusWatchlistDigComponent(): WatchlistDigComponent.Factory

  @CheckResult fun plusPortfolioDigComponent(): PortfolioDigComponent.Factory

  @Component.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance application: Application,
        @Named("debug") @BindsInstance debug: Boolean,
        @BindsInstance theming: Theming,
    ): TickerComponent
  }

  @Module
  abstract class TickerProvider {

    @Binds @CheckResult abstract fun bindTapePreferences(impl: PreferencesImpl): TapePreferences

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
      internal fun provideServiceClass(): Class<out Service> {
        return TapeService::class.java
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
    }
  }
}
