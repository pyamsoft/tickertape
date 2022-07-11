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

package com.pyamsoft.tickertape.main

import android.os.Bundle
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.ui.navigator.Navigator
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol

@CheckResult
private fun MainPage.defaultAsScreen(): Navigator.Screen<MainPage> {
  val self = this
  return object : Navigator.Screen<MainPage> {
    override val arguments: Bundle? = null
    override val screen: MainPage = self
  }
}

interface TopLevelScreen {

  val name: String

  @CheckResult fun asScreen(): Navigator.Screen<MainPage>
}

sealed class MainPage {
  object Home : MainPage(), TopLevelScreen {

    override val name = "Home"

    override fun asScreen(): Navigator.Screen<MainPage> = defaultAsScreen()
  }

  object WatchList : MainPage(), TopLevelScreen {

    override val name = "Watchlist"

    override fun asScreen(): Navigator.Screen<MainPage> = defaultAsScreen()
  }

  object Portfolio : MainPage(), TopLevelScreen {

    override val name = "Portfolio"
    override fun asScreen(): Navigator.Screen<MainPage> = defaultAsScreen()
  }

  object WatchListDig : MainPage() {

    const val KEY_SYMBOL = "key_symbol"
    const val KEY_LOOKUP_SYMBOL = "key_lookup_symbol"
    const val KEY_EQUITY_TYPE = "key_equity_type"
    const val KEY_ALLOW_MODIFY = "key_allow_modify"

    @CheckResult
    fun asScreen(
        symbol: StockSymbol,
        lookupSymbol: StockSymbol,
        equityType: EquityType,
        allowModifyWatchlist: Boolean,
    ): Navigator.Screen<MainPage> {
      val self = this
      return object : Navigator.Screen<MainPage> {
        override val arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.symbol())
              putString(KEY_LOOKUP_SYMBOL, lookupSymbol.symbol())
              putString(KEY_EQUITY_TYPE, equityType.name)
              putBoolean(KEY_ALLOW_MODIFY, allowModifyWatchlist)
            }
        override val screen = self
      }
    }
  }
}
