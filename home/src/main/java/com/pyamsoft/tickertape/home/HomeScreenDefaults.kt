package com.pyamsoft.tickertape.home

import android.content.res.Configuration
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object HomeScreenDefaults {

  @JvmStatic
  @Composable
  @CheckResult
  fun getItemWidth(): Dp {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      // Almost the whole screen in portrait
      configuration.screenWidthDp.dp * 0.9F
    } else {
      // Slightly more than half screen in landscape
      configuration.screenWidthDp.dp * 0.6F
    }
  }

  @JvmStatic
  @Composable
  @CheckResult
  fun getChartHeight(): Dp {
    val configuration = LocalConfiguration.current
    return if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      // One fourth the screen in portrait
      configuration.screenHeightDp.dp * 0.25F
    } else {
      // Slightly less than half screen in landscape
      configuration.screenHeightDp.dp * 0.45F
    }
  }
}
