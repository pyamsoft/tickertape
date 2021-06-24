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
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.alert.AlertModule
import com.pyamsoft.tickertape.alert.inject.AlertComponent
import com.pyamsoft.tickertape.alert.workmanager.WorkManagerModule
import com.pyamsoft.tickertape.db.DbModule
import com.pyamsoft.tickertape.db.room.RoomModule
import com.pyamsoft.tickertape.main.MainActivity
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.portfolio.PortfolioComponent
import com.pyamsoft.tickertape.portfolio.add.PortfolioAddComponent
import com.pyamsoft.tickertape.portfolio.item.PortfolioItemComponent
import com.pyamsoft.tickertape.portfolio.manage.ManageComponent
import com.pyamsoft.tickertape.portfolio.manage.add.PositionsAddComponent
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemComponent
import com.pyamsoft.tickertape.receiver.BootReceiver
import com.pyamsoft.tickertape.receiver.ScreenReceiver
import com.pyamsoft.tickertape.stocks.StockModule
import com.pyamsoft.tickertape.tape.TapeComponent
import com.pyamsoft.tickertape.tape.TapeModule
import com.pyamsoft.tickertape.tape.TapeService
import com.pyamsoft.tickertape.ui.UiModule
import com.pyamsoft.tickertape.watchlist.WatchlistComponent
import com.pyamsoft.tickertape.watchlist.WatchlistListComponent
import com.pyamsoft.tickertape.watchlist.add.WatchlistAddComponent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
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
            UiModule::class])
internal interface TickerComponent {

  /** Not actually used, just here so graph can compile */
  @CheckResult
  @Suppress("FunctionName")
  fun `$$daggerRequiredWatchlistListComponent`(): WatchlistListComponent.Factory

  /** Not actually used, just here so graph can compile */
  @CheckResult
  @Suppress("FunctionName")
  fun `$$daggerRequiredPositionItemComponent`(): PositionItemComponent.Factory

  /** Not actually used, just here so graph can compile */
  @CheckResult
  @Suppress("FunctionName")
  fun `$$daggerRequiredPortfolioItemComponent`(): PortfolioItemComponent.Factory

  // ===============================================
  // HACKY INJECTORS

  /* FROM inside BigMoverInjector, RefresherInjector: See TickerTape Injector */
  @CheckResult fun plusAlertComponent(): AlertComponent

  // ===============================================

  fun inject(receiver: BootReceiver)

  fun inject(receiver: ScreenReceiver)

  fun inject(application: TickerTape)

  @CheckResult fun plusPositionAddComponent(): PositionsAddComponent.Factory

  @CheckResult fun plusTapeComponent(): TapeComponent.Factory

  @CheckResult fun plusWatchlistAddComponent(): WatchlistAddComponent.Factory

  @CheckResult fun plusPortfolioAddComponent(): PortfolioAddComponent.Factory

  @CheckResult fun plusManageComponent(): ManageComponent.Factory

  @CheckResult fun plusMainComponent(): MainComponent.Factory

  @CheckResult fun plusWatchlistComponent(): WatchlistComponent.Factory

  @CheckResult fun plusPortfolioComponent(): PortfolioComponent.Factory

  @Component.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance application: Application,
        @Named("debug") @BindsInstance debug: Boolean,
        @BindsInstance theming: Theming,
        @BindsInstance imageLoader: ImageLoader,
    ): TickerComponent
  }

  @Module
  abstract class TickerProvider {

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
