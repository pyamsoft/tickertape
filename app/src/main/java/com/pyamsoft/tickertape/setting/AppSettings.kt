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

package com.pyamsoft.tickertape.setting

import android.os.Bundle
import android.view.View
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.preference.Preferences
import com.pyamsoft.pydroid.ui.settings.SettingsFragment
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.main.MainViewModeler
import javax.inject.Inject

internal class AppSettings : SettingsFragment() {

  override val hideClearAll: Boolean = false

  override val hideUpgradeInformation: Boolean = true

  @JvmField @Inject internal var mainViewModel: MainViewModeler? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Injector.obtainFromActivity<MainComponent>(requireActivity())
        .plusSettings()
        .create()
        .inject(this)

    mainViewModel.requireNotNull().restoreState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mainViewModel?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    mainViewModel = null
  }

  @Composable
  override fun customBottomItemMargin(): Dp {
    val state = mainViewModel.requireNotNull().state()

    val density = LocalDensity.current
    val height = state.bottomNavHeight
    return remember(density, height) { density.run { height.toDp() } }
  }

  @Composable
  override fun customPostPreferences(): List<Preferences> {
    return emptyList()
  }

  @Composable
  override fun customPrePreferences(): List<Preferences> {
    return emptyList()
  }

  @Composable
  override fun customTopItemMargin(): Dp {
    return 0.dp
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return AppSettings().apply { arguments = Bundle.EMPTY }
    }
  }
}
