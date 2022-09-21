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

package com.pyamsoft.tickertape.watchlist

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullWidth
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.quote.DeleteTicker
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import javax.inject.Inject

internal class WatchlistRemoveDialog : AppCompatDialogFragment() {

  @JvmField @Inject internal var presenter: WatchlistDeletePresenter? = null
  @JvmField @Inject internal var theming: Theming? = null

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireArguments()
        .getString(KEY_SYMBOL)
        .let { it.requireNotNull { "Must be created with $KEY_SYMBOL" } }
        .asSymbol()
  }

  private fun handleDelete(symbol: StockSymbol) {
    presenter
        .requireNotNull()
        .handleRemove(
            scope = requireActivity().lifecycleScope,
            symbol = symbol,
            onRemoved = { dismiss() },
        )
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val act = requireActivity()
    Injector.obtainFromApplication<TickerComponent>(act).inject(this)

    val symbol = getSymbol()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.dialog_watchlist_dig

      setContent {
        act.TickerTapeTheme(themeProvider) {
          DeleteTicker(
              modifier = Modifier.fillMaxWidth(),
              symbol = symbol,
              onCancel = { dismiss() },
              onConfirm = { handleDelete(symbol) },
          )
        }
      }
    }
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullWidth()
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    makeFullWidth()
    recompose()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    presenter = null
    theming = null
  }

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    private val TAG = WatchlistRemoveDialog::class.java.name

    @JvmStatic
    @CheckResult
    private fun newInstance(
        symbol: StockSymbol,
    ): DialogFragment {
      return WatchlistRemoveDialog().apply {
        arguments = Bundle().apply { putString(KEY_SYMBOL, symbol.raw) }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        symbol: StockSymbol,
    ) {
      newInstance(symbol).show(activity, TAG)
    }
  }
}
