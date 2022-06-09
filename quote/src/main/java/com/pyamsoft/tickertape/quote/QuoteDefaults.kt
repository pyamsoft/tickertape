package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object QuoteDefaults {
  @JvmStatic
  @Composable
  @CheckResult
  fun getChartHeight(fraction: Float = 0.25F): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp * fraction
  }
}
