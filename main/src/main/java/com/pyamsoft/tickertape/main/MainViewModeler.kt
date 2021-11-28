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

package com.pyamsoft.tickertape.main

import android.app.Activity
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.core.ActivityScope
import javax.inject.Inject

class MainViewModeler
@Inject
internal constructor(
    private val state: MutableMainViewState,
    private val theming: Theming,
) : AbstractViewModeler<MainViewState>(state) {

  fun handleMeasureBottomNavHeight(height: Int) {
    state.bottomNavHeight = height
  }

  fun handleSyncDarkTheme(activity: Activity) {
    val isDark = theming.isDarkTheme(activity)
    state.theme = if (isDark) Theming.Mode.DARK else Theming.Mode.LIGHT
  }

  override fun saveState(outState: UiSavedStateWriter) {
    state.theme.also { theme ->
      if (theme != Theming.Mode.SYSTEM) {
        outState.put(KEY_THEME, theme.name)
      } else {
        outState.remove(KEY_THEME)
      }
    }

    state.bottomNavHeight.also { height ->
      if (height > 0) {
        outState.put(KEY_BOTTOM_NAV, height)
      } else {
        outState.remove(KEY_BOTTOM_NAV)
      }
    }
  }

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    savedInstanceState.get<String>(KEY_THEME)?.also { themeName ->
      val theme = Theming.Mode.valueOf(themeName)
      state.theme = theme
    }

    savedInstanceState.get<Int>(KEY_BOTTOM_NAV)?.also { height -> state.bottomNavHeight = height }
  }

  companion object {

    private const val KEY_THEME = "theme"
    private const val KEY_BOTTOM_NAV = "bottom_nav"
  }
}
