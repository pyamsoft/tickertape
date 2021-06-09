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

import android.app.Application
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.db.DbModule
import com.pyamsoft.tickertape.db.room.RoomModule
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.main.add.SymbolAddComponent
import com.pyamsoft.tickertape.quote.QuoteComponent
import com.pyamsoft.tickertape.stocks.StockModule
import com.pyamsoft.tickertape.ui.UiModule
import com.pyamsoft.tickertape.watchlist.WatchlistComponent
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
            UiModule::class])
internal interface TickerComponent {

  /** Not actually used, just here so graph can compile */
  @CheckResult
  @Suppress("FunctionName")
  fun `$$daggerRequiredQuoteComponent`(): QuoteComponent.Factory

  @CheckResult fun plusSymbolAddComponent(): SymbolAddComponent.Factory

  @CheckResult fun plusMainComponent(): MainComponent.Factory

  @CheckResult fun plusWatchListComponent(): WatchlistComponent.Factory

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
