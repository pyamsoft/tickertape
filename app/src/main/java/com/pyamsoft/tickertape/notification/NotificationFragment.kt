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

package com.pyamsoft.tickertape.notification

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.pydroid.ui.navigator.Navigator
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.main.MainViewModeler
import com.pyamsoft.tickertape.main.TopLevelMainPage
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import javax.inject.Inject

class NotificationFragment : Fragment(), FragmentNavigator.Screen<MainPage> {

  @JvmField @Inject internal var navigator: Navigator<MainPage>? = null
  @JvmField @Inject internal var viewModel: NotificationViewModeler? = null
  @JvmField @Inject internal var mainViewModel: MainViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null

  private fun handleTapePageSizeChanged(size: Int) {
    viewModel
        .requireNotNull()
        .handleTapePageSizeChanged(
            scope = viewLifecycleOwner.lifecycleScope,
            size = size,
        )
  }

  private fun handleTapeNotificationToggled() {
    viewModel
        .requireNotNull()
        .handleTapeNotificationToggled(
            scope = viewLifecycleOwner.lifecycleScope,
        )
  }

    private fun handleBigMoverNotificationToggled() {
        viewModel
            .requireNotNull()
            .handleBigMoverNotificationToggled(
                scope = viewLifecycleOwner.lifecycleScope,
            )
    }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val act = requireActivity()
    Injector.obtainFromActivity<MainComponent>(act).plusAlerts().create().inject(this)

    val vm = viewModel.requireNotNull()
    val mainVM = mainViewModel.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.screen_notifications

      setContent {
        vm.Render { state ->
          mainVM.Render { mainState ->
            act.TickerTapeTheme(themeProvider) {
              NotificationScreen(
                  modifier = Modifier.fillMaxSize(),
                  state = state,
                  navBarBottomHeight = mainState.bottomNavHeight,
                  onTapeNotificationToggled = { handleTapeNotificationToggled() },
                  onTapePageSizeChanged = { handleTapePageSizeChanged(it) },
                  onBigMoverNotificationToggled = { handleBigMoverNotificationToggled() },
              )
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

    viewModel = null
    mainViewModel = null
    theming = null
    navigator = null
  }

  override fun getScreenId(): MainPage {
    return TopLevelMainPage.Notifications
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return NotificationFragment().apply { arguments = Bundle.EMPTY }
    }
  }
}