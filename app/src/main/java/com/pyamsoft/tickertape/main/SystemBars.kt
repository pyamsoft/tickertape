/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.getSystemDarkMode

@Composable
internal fun ComponentActivity.SystemBars(
    theme: Theming.Mode,
) {
  // Dark icons in Light mode only
  val isDarkMode = theme.getSystemDarkMode()

  val darkIcons = remember(isDarkMode) { !isDarkMode }

  val view = LocalView.current
  val w = window
  val controller =
      remember(
          w,
          view,
      ) {
        WindowInsetsControllerCompat(w, view)
      }
  LaunchedEffect(
      isDarkMode,
      darkIcons,
      controller,
  ) {
    val style =
        if (isDarkMode) SystemBarStyle.dark(Color.TRANSPARENT)
        else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(
        statusBarStyle = style,
        navigationBarStyle = style,
    )
    controller.isAppearanceLightStatusBars = darkIcons
    controller.isAppearanceLightNavigationBars = isDarkMode
  }
}
