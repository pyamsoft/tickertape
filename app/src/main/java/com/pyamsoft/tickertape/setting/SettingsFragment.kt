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
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.UnitControllerEvent
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.requireAppBarActivity
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.settings.AppSettingsFragment
import com.pyamsoft.pydroid.ui.settings.AppSettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.applyAppBarOffset
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import javax.inject.Inject

internal class SettingsFragment : AppSettingsFragment() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.post { view.applyAppBarOffset(requireAppBarActivity(), viewLifecycleOwner) }
  }

  override fun provideSettingsFragment(): AppSettingsPreferenceFragment {
    return SettingsPreferenceFragment()
  }

  override fun provideSettingsTag(): String {
    return SettingsPreferenceFragment.TAG
  }

  companion object {

    const val TAG = "SettingsFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return SettingsFragment().apply { arguments = Bundle().apply {} }
    }
  }

  internal class SettingsPreferenceFragment :
      AppSettingsPreferenceFragment(), UiController<UnitControllerEvent> {

    override val preferenceXmlResId = 0

    override val hideUpgradeInformation = true

    @JvmField @Inject internal var factory: TickerViewModelFactory? = null
    private val viewModel by fromViewModelFactory<PreferenceViewModel>(activity = true) {
      factory?.create(requireActivity())
    }

    @JvmField @Inject internal var bottomSpacer: SettingsBottomSpacer? = null

    private var stateSaver: StateSaver? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      Injector.obtainFromApplication<TickerComponent>(view.context)
          .plusPreferenceComponent()
          .create(preferenceScreen)
          .inject(this)

      stateSaver =
          createComponent(
              savedInstanceState,
              viewLifecycleOwner,
              viewModel,
              this,
              requireNotNull(bottomSpacer)) {}
    }

    override fun onControllerEvent(event: UnitControllerEvent) {}

    override fun onSaveInstanceState(outState: Bundle) {
      super.onSaveInstanceState(outState)
      stateSaver?.saveState(outState)
    }

    override fun onDestroyView() {
      super.onDestroyView()
      factory = null
      stateSaver = null
    }

    companion object {

      const val TAG = "SettingsPreferenceFragment"
    }
  }
}
