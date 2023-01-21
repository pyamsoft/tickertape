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

package com.pyamsoft.tickertape.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import coil.ImageLoader
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.pydroid.ui.navigator.Navigator
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.main.MainViewModeler
import com.pyamsoft.tickertape.main.TopLevelMainPage
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigFragment
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject

class HomeFragment : Fragment(), FragmentNavigator.Screen<MainPage> {

  @JvmField @Inject internal var navigator: Navigator<MainPage>? = null
  @JvmField @Inject internal var mainViewModel: MainViewModeler? = null
  @JvmField @Inject internal var viewModel: HomeViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  private fun onOpenDigDialog(ticker: Ticker) {
    val quote = ticker.quote
    if (quote == null) {
      Timber.w("Can't show dig dialog, missing quote: ${ticker.symbol}")
      return
    }

    val equityType = quote.type
    val lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol else quote.symbol
    navigator
        .requireNotNull()
        .navigateTo(
            WatchlistDigFragment.Screen(
                symbol = ticker.symbol,
                lookupSymbol = lookupSymbol,
                equityType = equityType,
            ),
        )
  }

  private fun onOpenSettingsDialog() {
    SettingsDialog.show(requireActivity())
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val act = requireActivity()

    ObjectGraph.ActivityScope.retrieve(act).plusHome().create().inject(this)

    val appName = act.getString(R.string.app_name)
    val vm = viewModel.requireNotNull()
    val mainVM = mainViewModel.requireNotNull()
    val loader = imageLoader.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.screen_home

      setContent {
        val handleOpenSettings by rememberUpdatedState { onOpenSettingsDialog() }

        val handleChartClicked by rememberUpdatedState { ticker: Ticker -> onOpenDigDialog(ticker) }

        val handleRefreshWatchlist by
            rememberUpdatedState<CoroutineScope.() -> Unit> { vm.handleFetchWatchlist(this, false) }

        val handleRefreshUndervaluedGrowth by
            rememberUpdatedState<CoroutineScope.() -> Unit> {
              vm.handleFetchUndervaluedGrowth(this, false)
            }

        val handleRefreshTrending by
            rememberUpdatedState<CoroutineScope.() -> Unit> { vm.handleFetchTrending(this, false) }

        val handleRefreshPortfolio by
            rememberUpdatedState<CoroutineScope.() -> Unit> { vm.handleFetchPortfolio(this, false) }

        val handleRefreshMostShorted by
            rememberUpdatedState<CoroutineScope.() -> Unit> {
              vm.handleFetchMostShorted(this, false)
            }

        val handleRefreshLosers by
            rememberUpdatedState<CoroutineScope.() -> Unit> { vm.handleFetchLosers(this, false) }

        val handleRefreshGainers by
            rememberUpdatedState<CoroutineScope.() -> Unit> { vm.handleFetchGainers(this, false) }

        val handleRefreshIndexes by
            rememberUpdatedState<CoroutineScope.() -> Unit> { vm.handleFetchIndexes(this, false) }

        val handleRefreshGrowthTech by
            rememberUpdatedState<CoroutineScope.() -> Unit> {
              vm.handleFetchGrowthTech(this, false)
            }

        val handleRefreshMostActive by
            rememberUpdatedState<CoroutineScope.() -> Unit> {
              vm.handleFetchMostActive(this, false)
            }

        act.TickerTapeTheme(themeProvider) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                state = vm.state,
                appName = appName,
                imageLoader = loader,
                navBarBottomHeight = mainVM.state.bottomNavHeight,
                onSettingsClicked = handleOpenSettings,
                onChartClicked = handleChartClicked,
                onRefreshWatchlist = handleRefreshWatchlist,
                onRefreshUndervaluedGrowth = handleRefreshUndervaluedGrowth,
                onRefreshTrending = handleRefreshTrending,
                onRefreshPortfolio = handleRefreshPortfolio,
                onRefreshMostShorted = handleRefreshMostShorted,
                onRefreshLosers = handleRefreshLosers,
                onRefreshIndexes = handleRefreshIndexes,
                onRefreshGrowthTech = handleRefreshGrowthTech,
                onRefreshGainers = handleRefreshGainers,
                onRefreshMostActive = handleRefreshMostActive,
            )
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mainViewModel.requireNotNull().restoreState(savedInstanceState)
    viewModel.requireNotNull().restoreState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
    mainViewModel?.saveState(outState)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    recompose()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    theming = null
    viewModel = null
    mainViewModel = null
    imageLoader = null
    navigator = null
  }

  override fun getScreenId(): MainPage {
    return TopLevelMainPage.Home
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return HomeFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
