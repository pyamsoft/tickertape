package com.pyamsoft.tickertape.home

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object HomeScreenDefaults {

  @JvmStatic
  @Composable
  @CheckResult
  fun getItemWidth(fraction: Float = 0.9F): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp * fraction
  }

  @JvmStatic
  @Composable
  @CheckResult
  fun getChartHeight(fraction: Float = 0.25F): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp * fraction
  }
}
