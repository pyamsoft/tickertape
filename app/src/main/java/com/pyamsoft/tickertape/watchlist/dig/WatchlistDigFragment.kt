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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import javax.inject.Inject

internal class WatchlistDigFragment : Fragment() {

  @JvmField @Inject internal var viewModel: WatchlistDigViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  private var windowInsetObserver: ViewWindowInsetObserver? = null

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireArguments()
        .getString(MainPage.WatchListDig.KEY_SYMBOL)
        .let { it.requireNotNull { "Must be created with ${MainPage.WatchListDig.KEY_SYMBOL}" } }
        .asSymbol()
  }

  @CheckResult
  private fun getLookupSymbol(): StockSymbol {
    return requireArguments()
        .getString(MainPage.WatchListDig.KEY_LOOKUP_SYMBOL)
        .let {
          it.requireNotNull { "Must be created with ${MainPage.WatchListDig.KEY_LOOKUP_SYMBOL}" }
        }
        .asSymbol()
  }

  @CheckResult
  private fun getEquityType(): EquityType {
    return requireArguments()
        .getString(MainPage.WatchListDig.KEY_EQUITY_TYPE)
        .let {
          it.requireNotNull { "Must be created with ${MainPage.WatchListDig.KEY_EQUITY_TYPE}" }
        }
        .let { EquityType.valueOf(it) }
  }

  @CheckResult
  private fun getAllowModifyWatchlist(): Boolean {
    val args = requireArguments()
    if (!args.containsKey(MainPage.WatchListDig.KEY_ALLOW_MODIFY)) {
      throw IllegalArgumentException(
          "Must be created with ${MainPage.WatchListDig.KEY_ALLOW_MODIFY}")
    }

    return args.getBoolean(MainPage.WatchListDig.KEY_ALLOW_MODIFY, false)
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

      val observer = ViewWindowInsetObserver(this)
      val windowInsets = observer.start()
      windowInsetObserver = observer

      setContent {
        vm.Render { state ->
          act.TickerTapeTheme(themeProvider) {
            CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
              WatchlistDigScreen(
                  modifier = Modifier.fillMaxWidth(),
                  state = state,
                  imageLoader = loader,
                  onClose = { act.onBackPressed() },
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
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.requireNotNull().restoreState(savedInstanceState)
    handleRefresh(force = false)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    recompose()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    windowInsetObserver?.stop()
    windowInsetObserver = null

    viewModel = null
    theming = null
    imageLoader = null
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(
        // Nullable for navigator API compat
        bundle: Bundle?
    ): Fragment {
      return WatchlistDigFragment().apply {
        arguments =
            bundle.requireNotNull { "WatchlistDigFragment must be created with argument Bundle" }
      }
    }
  }
}
