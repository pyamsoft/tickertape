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
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.main.MainViewModeler
import com.pyamsoft.tickertape.main.TopLevelMainPage
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import javax.inject.Inject

class HomeFragment : Fragment(), FragmentNavigator.Screen<MainPage> {

  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var mainViewModel: MainViewModeler? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val act = requireActivity()

    ObjectGraph.ActivityScope.retrieve(act).plusHome().create().inject(this)

    val appName = act.getString(R.string.app_name)

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.screen_home

      setContent {
        act.TickerTapeTheme(themeProvider) {
          HomeEntry(
              modifier = Modifier.fillMaxSize(),
              appName = appName,
              onOpenWatchlistDig = {
                mainViewModel
                    .requireNotNull()
                    .handleOpenDig(
                        scope = viewLifecycleOwner.lifecycleScope,
                        ticker = it,
                    )
              },
          )
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    theming = null
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
