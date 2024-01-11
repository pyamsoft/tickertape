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

package com.pyamsoft.tickertape.ui

import android.content.res.Configuration
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@CheckResult
private fun rememberScreenRatio(
    totalSizeInDp: Int,
    orientation: Int,
    ratioPortrait: Float,
    ratioLandscape: Float
): Dp {
  return remember(
      orientation,
      totalSizeInDp,
      ratioPortrait,
      ratioLandscape,
  ) {
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      // Almost the whole screen in portrait
      totalSizeInDp.dp * ratioPortrait
    } else {
      totalSizeInDp.dp * ratioLandscape
    }
  }
}

/** Get a size relative to the current Screen width based on Orientation */
@Composable
@CheckResult
fun rememberScreenWidthRatio(
    ratioPortrait: Float,
    ratioLandscape: Float,
): Dp {
  val configuration = LocalConfiguration.current
  val orientation = configuration.orientation
  val screenWidth = configuration.screenWidthDp
  return rememberScreenRatio(
      totalSizeInDp = screenWidth,
      orientation = orientation,
      ratioPortrait = ratioPortrait,
      ratioLandscape = ratioLandscape,
  )
}

/** Get a size relative to the current Screen height based on Orientation */
@Composable
@CheckResult
fun rememberScreenHeightRatio(
    ratioPortrait: Float,
    ratioLandscape: Float,
): Dp {
  val configuration = LocalConfiguration.current
  val orientation = configuration.orientation
  val screenHeight = configuration.screenHeightDp
  return rememberScreenRatio(
      totalSizeInDp = screenHeight,
      orientation = orientation,
      ratioPortrait = ratioPortrait,
      ratioLandscape = ratioLandscape,
  )
}
