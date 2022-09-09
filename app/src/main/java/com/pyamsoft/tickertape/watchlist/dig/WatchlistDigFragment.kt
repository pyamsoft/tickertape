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
import androidx.activity.compose.BackHandler
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.navigator.BackstackNavigator
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import javax.inject.Inject

internal class WatchlistDigFragment : Fragment(), FragmentNavigator.Screen<MainPage> {

  @JvmField @Inject internal var navigator: BackstackNavigator<MainPage>? = null
  @JvmField @Inject internal var viewModel: WatchlistDigViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  private fun handleRecommendationSelected(ticker: Ticker) {
    val quote = ticker.quote

    navigator
        .requireNotNull()
        .navigateTo(
            Screen(
                symbol = ticker.symbol,
                lookupSymbol = ticker.symbol,
                equityType = quote?.type ?: EquityType.STOCK,
            ),
        )
  }

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
        .handleChartRangeSelected(
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
    Injector.obtainFromActivity<MainComponent>(act)
        .plusWatchlistDig()
        .create(
            getSymbol(),
            getLookupSymbol(),
            getEquityType(),
        )
        .inject(this)

    val vm = viewModel.requireNotNull()
    val loader = imageLoader.requireNotNull()
    val navi = navigator.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.dialog_watchlist_dig

      setContent {
        vm.Render { state ->
          act.TickerTapeTheme(themeProvider) {
            BackHandler(
                onBack = { navi.goBack() },
            )

            WatchlistDigScreen(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                imageLoader = loader,
                onClose = { act.onBackPressedDispatcher.onBackPressed() },
                onChartScrub = { vm.handleChartDateScrubbed(it) },
                onChartRangeSelected = { handleRangeSelected(it) },
                onModifyWatchlist = { handleModifyWatchlist() },
                onRefresh = { handleRefresh(true) },
                onTabUpdated = { handleTabUpdated(it) },
                onRecClick = { handleRecommendationSelected(it) },
                onOptionSectionChanged = { vm.handleOptionsSectionChanged(it) },
                onOptionExpirationDateChanged = { vm.handleOptionsExpirationDateChanged(it) },
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

    viewModel = null
    theming = null
    imageLoader = null
    navigator = null
  }

  override fun getScreenId(): MainPage {
    return Screen(
        symbol = getSymbol(),
        lookupSymbol = getLookupSymbol(),
        equityType = getEquityType(),
    )
  }

  data class Screen(
      val symbol: StockSymbol,
      val lookupSymbol: StockSymbol,
      val equityType: EquityType,
  ) : MainPage

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    private const val KEY_LOOKUP_SYMBOL = "key_lookup_symbol"
    private const val KEY_EQUITY_TYPE = "key_equity_type"

    @JvmStatic
    @CheckResult
    fun newInstance(
        symbol: StockSymbol,
        lookupSymbol: StockSymbol,
        equityType: EquityType,
    ): Fragment {
      return WatchlistDigFragment().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.raw)
              putString(KEY_LOOKUP_SYMBOL, lookupSymbol.raw)
              putString(KEY_EQUITY_TYPE, equityType.name)
            }
      }
    }
  }
}
