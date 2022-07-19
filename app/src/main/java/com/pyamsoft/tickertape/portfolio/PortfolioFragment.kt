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

package com.pyamsoft.tickertape.portfolio

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
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
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
import com.pyamsoft.tickertape.main.TopLevelMainPage
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigFragment
import com.pyamsoft.tickertape.quote.add.TickerDestination
import com.pyamsoft.tickertape.ticker.add.NewTickerSheet
import javax.inject.Inject

class PortfolioFragment : Fragment(), FragmentNavigator.Screen<MainPage> {

  @JvmField @Inject internal var navigator: Navigator<MainPage>? = null
  @JvmField @Inject internal var viewModel: PortfolioViewModeler? = null
  @JvmField @Inject internal var mainViewModel: MainViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  private var windowInsetObserver: ViewWindowInsetObserver? = null

  private fun handleOpenManageDialog(stock: PortfolioStock) {
    val quote = stock.ticker?.quote
    val session = quote?.currentSession

    navigator
        .requireNotNull()
        .navigateTo(
            PortfolioDigFragment.Screen.create(
                holding = stock.holding,
                quote = quote,
                currentPrice = session?.price,
            ),
        )
  }

  private fun handleDeleteStock(stock: PortfolioStock) {
    PortfolioRemoveDialog.show(
        requireActivity(),
        holding = stock.holding,
    )
  }

  private fun handleRefresh(force: Boolean) {
    viewModel
        .requireNotNull()
        .handleRefreshList(
            scope = viewLifecycleOwner.lifecycleScope,
            force = force,
        )
  }

  private fun handleFabClicked() {
    NewTickerSheet.show(
        requireActivity(),
        TickerDestination.PORTFOLIO,
    )
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val act = requireActivity()
    Injector.obtainFromActivity<MainComponent>(act).plusPortfolio().create().inject(this)

    val vm = viewModel.requireNotNull()
    val mainVM = mainViewModel.requireNotNull()
    val loader = imageLoader.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.screen_portfolio

      val observer = ViewWindowInsetObserver(this)
      val windowInsets = observer.start()
      windowInsetObserver = observer

      setContent {
        vm.Render { state ->
          mainVM.Render { mainState ->
            act.TickerTapeTheme(themeProvider) {
              CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
                PortfolioScreen(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    imageLoader = loader,
                    navBarBottomHeight = mainState.bottomNavHeight,
                    onRefresh = { handleRefresh(true) },
                    onSelect = { handleOpenManageDialog(it) },
                    onDelete = { handleDeleteStock(it) },
                    onSearchChanged = { vm.handleSearch(it) },
                    onTabUpdated = { vm.handleSectionChanged(it) },
                    onFabClick = { handleFabClicked() },
                    onRegenerateList = { vm.handleRegenerateList(this) },
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
    viewModel.requireNotNull().also { vm ->
      vm.restoreState(savedInstanceState)
      vm.bind(scope = viewLifecycleOwner.lifecycleScope)
    }
    mainViewModel.requireNotNull().restoreState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    handleRefresh(force = false)
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

    viewModel = null
    mainViewModel = null
    theming = null
    imageLoader = null
  }

  override fun getScreenId(): MainPage {
    return TopLevelMainPage.Portfolio
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return PortfolioFragment().apply { arguments = Bundle.EMPTY }
    }
  }
}
