package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.pyamsoft.tickertape.ui.rememberScreenHeightRatio
import com.pyamsoft.tickertape.ui.rememberScreenWidthRatio

internal object HomeScreenDefaults {

  @JvmStatic
  @Composable
  @CheckResult
  fun rememberItemWidth(): Dp {
    return rememberScreenWidthRatio(
        // Almost the whole screen in portrait
        ratioPortrait = 0.9F,
        // Slightly more than half screen in landscape
        ratioLandscape = 0.6F,
    )
  }

  @JvmStatic
  @Composable
  @CheckResult
  fun rememberChartHeight(): Dp {
    return rememberScreenHeightRatio(
        // One fourth the screen in portrait
        ratioPortrait = 0.25F,
        // Slightly less than half screen in landscape
        ratioLandscape = 0.45F,
    )
  }
}
