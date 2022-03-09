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

package com.pyamsoft.tickertape.watchlist.dig

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
import coil.ImageLoader
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
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import javax.inject.Inject

internal class WatchlistDigDialog : AppCompatDialogFragment() {

  @JvmField @Inject internal var viewModel: WatchlistDigViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireArguments()
        .getString(KEY_SYMBOL)
        .let { it.requireNotNull { "Must be created with $KEY_SYMBOL" } }
        .asSymbol()
  }

  @CheckResult
  private fun getLookupSymbol(): StockSymbol {
    return requireArguments()
        .getString(KEY_LOOKUP_SYMBOL)
        .let { it.requireNotNull { "Must be created with $KEY_LOOKUP_SYMBOL" } }
        .asSymbol()
  }

  @CheckResult
  private fun getEquityType(): EquityType {
    return requireArguments()
        .getString(KEY_EQUITY_TYPE)
        .let { it.requireNotNull { "Must be created with $KEY_EQUITY_TYPE" } }
        .let { EquityType.valueOf(it) }
  }

  @CheckResult
  private fun getAllowModifyWatchlist(): Boolean {
    val args = requireArguments()
    if (!args.containsKey(KEY_ALLOW_MODIFY)) {
      throw IllegalArgumentException("Must be created with $KEY_ALLOW_MODIFY")
    }

    return args.getBoolean(KEY_ALLOW_MODIFY, false)
  }

  private fun handleModifyWatchlist() {
    viewModel
        .requireNotNull()
        .handleModifyWatchlist(
            scope = viewLifecycleOwner.lifecycleScope,
        )
  }

  private fun handleRangeSelected(range: StockChart.IntervalRange) {
    viewModel
        .requireNotNull()
        .handleRangeSelected(
            scope = viewLifecycleOwner.lifecycleScope,
            range = range,
        )
  }

  private fun handleTabUpdated(section: WatchlistDigSections) {
    viewModel
        .requireNotNull()
        .handleTabUpdated(
            scope = viewLifecycleOwner.lifecycleScope,
            section = section,
        )
  }

  private fun handleRefresh(force: Boolean) {
    viewModel
        .requireNotNull()
        .handleLoadTicker(
            scope = viewLifecycleOwner.lifecycleScope,
            force = force,
        )
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val act = requireActivity()
    Injector.obtainFromApplication<TickerComponent>(act)
        .plusWatchlistDigComponent()
        .create(
            getSymbol(),
            getLookupSymbol(),
            getAllowModifyWatchlist(),
            getEquityType(),
        )
        .inject(this)

    val vm = viewModel.requireNotNull()
    val loader = imageLoader.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.dialog_watchlist_dig

      setContent {
        vm.Render { state ->
          act.TickerTapeTheme(themeProvider) {
            WatchlistDigScreen(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                imageLoader = loader,
                onClose = { dismiss() },
                onScrub = { vm.handleDateScrubbed(it) },
                onRangeSelected = { handleRangeSelected(it) },
                onModifyWatchlist = { handleModifyWatchlist() },
                onRefresh = { handleRefresh(true) },
                onTabUpdated = { handleTabUpdated(it) },
            )
          }
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

    viewModel.requireNotNull().restoreState(savedInstanceState)
    handleRefresh(force = false)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    makeFullWidth()
    recompose()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    viewModel = null
    theming = null
    imageLoader = null
  }

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    private const val KEY_LOOKUP_SYMBOL = "key_lookup_symbol"
    private const val KEY_EQUITY_TYPE = "key_equity_type"
    private const val KEY_ALLOW_MODIFY = "key_allow_modify"
    private const val TAG = "WatchlistDigDialog"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        symbol: StockSymbol,
        lookupSymbol: StockSymbol,
        equityType: EquityType,
        allowModifyWatchlist: Boolean,
    ): DialogFragment {
      return WatchlistDigDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.symbol())
              putString(KEY_LOOKUP_SYMBOL, lookupSymbol.symbol())
              putString(KEY_EQUITY_TYPE, equityType.name)
              putBoolean(KEY_ALLOW_MODIFY, allowModifyWatchlist)
            }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        symbol: StockSymbol,
        lookupSymbol: StockSymbol,
        equityType: EquityType,
        allowModifyWatchlist: Boolean,
    ) {
      newInstance(
              symbol,
              lookupSymbol,
              equityType,
              allowModifyWatchlist,
          )
          .show(activity, TAG)
    }
  }
}
