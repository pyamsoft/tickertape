package com.pyamsoft.tickertape.quote.dig

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.pyamsoft.tickertape.ui.rememberScreenHeightRatio

internal object DigDefaults {

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
