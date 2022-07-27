package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle

enum class TickerSize {
  CHART,
  QUOTE
}

internal data class TickerSizes
internal constructor(
    val title: TextStyle,
    val description: TextStyle,
) {

  companion object {
    @JvmStatic
    @CheckResult
    fun chart(typography: Typography) =
        TickerSizes(
            title = typography.body1,
            description = typography.body2,
        )

    @JvmStatic
    @CheckResult
    fun company(typography: Typography) =
        TickerSizes(
            title = typography.h6,
            description = typography.body1,
        )

    @JvmStatic
    @CheckResult
    fun price(typography: Typography) =
        TickerSizes(
            title = typography.h5,
            description = typography.body2,
        )
  }
}
