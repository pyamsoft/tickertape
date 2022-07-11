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
import com.pyamsoft.pydroid.ui.navigator.Navigator
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.main.MainViewModeler
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.setting.SettingsDialog
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import javax.inject.Inject
import timber.log.Timber

class HomeFragment : Fragment() {

  @JvmField @Inject internal var navigator: Navigator<MainPage>? = null
  @JvmField @Inject internal var mainViewModel: MainViewModeler? = null
  @JvmField @Inject internal var viewModel: HomeViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  private var windowInsetObserver: ViewWindowInsetObserver? = null

  private fun handleOpenDigDialog(ticker: Ticker) {
    val quote = ticker.quote
    if (quote == null) {
      Timber.w("Can't show dig dialog, missing quote: ${ticker.symbol}")
      return
    }

    val equityType = quote.type()
    val lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol() else quote.symbol()
    navigator
        .requireNotNull()
        .navigateTo(
            MainPage.WatchListDig.asScreen(
                symbol = ticker.symbol,
                lookupSymbol = lookupSymbol,
                allowModifyWatchlist = false,
                equityType = equityType,
            ),
        )
  }

  private fun handleOpenSettingsDialog() {
    SettingsDialog.show(requireActivity())
  }

  private fun handleRefresh(force: Boolean) {
    viewModel
        .requireNotNull()
        .handleRefreshList(
            scope = viewLifecycleOwner.lifecycleScope,
            force = force,
        )
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val act = requireActivity()
    Injector.obtainFromActivity<MainComponent>(act).plusHome().create().inject(this)

    val vm = viewModel.requireNotNull()
    val mainVM = mainViewModel.requireNotNull()
    val loader = imageLoader.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.screen_home

      val observer = ViewWindowInsetObserver(this)
      val windowInsets = observer.start()
      windowInsetObserver = observer

      val appName = act.getString(R.string.app_name)

      setContent {
        vm.Render { state ->
          mainVM.Render { mainState ->
            act.TickerTapeTheme(themeProvider) {
              CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    appName = appName,
                    imageLoader = loader,
                    navBarBottomHeight = mainState.bottomNavHeight,
                    onRefresh = { handleRefresh(true) },
                    onSettingsClicked = { handleOpenSettingsDialog() },
                    onChartClicked = { handleOpenDigDialog(it) },
                )
              }
            }
          }
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.requireNotNull().restoreState(savedInstanceState)
    mainViewModel.requireNotNull().restoreState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    handleRefresh(false)
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

    windowInsetObserver?.stop()
    windowInsetObserver = null

    theming = null
    viewModel = null
    mainViewModel = null
    imageLoader = null
    navigator = null
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return HomeFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
