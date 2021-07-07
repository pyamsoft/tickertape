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

import androidx.preference.PreferenceScreen
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UnitViewEvent
import com.pyamsoft.pydroid.ui.arch.PrefUiView
import javax.inject.Inject

class SettingsSpacer
@Inject
internal constructor(
    private val parent: PreferenceScreen,
) : PrefUiView<SettingsViewState, UnitViewEvent>(parent) {

  private var space: PreferenceBottomSpace? = null

  private fun addSpacer(height: Int) {
    space?.let { preference -> parent.removePreference(preference) }
    space =
        PreferenceBottomSpace(height, parent.context).also { preference ->
          parent.addPreference(preference)
        }
  }

  override fun onRender(state: UiRender<SettingsViewState>) {
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomMargin(it) }
  }

  private fun handleBottomMargin(height: Int) {
    addSpacer(height)
  }
}
