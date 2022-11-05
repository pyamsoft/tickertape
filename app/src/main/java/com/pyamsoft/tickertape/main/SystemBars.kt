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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pyamsoft.pydroid.ui.theme.Theming

@Composable
internal fun SystemBars(
    theme: Theming.Mode,
    page: MainPage?,
) {
  val isDigPage = remember(page) { page != null && page !is TopLevelMainPage }

  // Dark icons in Light mode only
  var darkIcons =
      if (theme == Theming.Mode.SYSTEM) !isSystemInDarkTheme() else theme == Theming.Mode.LIGHT

  // In light mode, when we dig, we should make the icons bright or else it looks weird
  if (darkIcons) {
    if (isDigPage) {
      darkIcons = false
    }
  }

  val controller = rememberSystemUiController()
  SideEffect {
    controller.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = darkIcons,
        isNavigationBarContrastEnforced = false,
    )
  }
}
