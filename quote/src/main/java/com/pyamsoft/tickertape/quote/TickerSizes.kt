package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class TickerSize {
  CHART,
  QUOTE,
  QUOTE_SPECIAL
}

data class TickerSizes
internal constructor(
    val title: TextStyle,
    val description: TextStyle,
    val label: TextStyle,
) {

  companion object {
    @JvmStatic
    @CheckResult
    fun chart(typography: Typography, color: Color) =
        TickerSizes(
            title = typography.body1.copy(color = color),
            description = typography.body2.copy(color = color),
            label = typography.caption.copy(color = color),
        )

    @JvmStatic
    @CheckResult
    fun company(typography: Typography, color: Color) =
        TickerSizes(
            title =
                typography.h6.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                ),
            description =
                typography.body2.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                ),
            label = typography.caption.copy(color = color),
        )

    @JvmStatic
    @CheckResult
    fun price(typography: Typography, color: Color) =
        TickerSizes(
            title =
                typography.h5.copy(
                    color = color,
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body2.copy(
                    color = color,
                    fontWeight = FontWeight.W600,
                ),
            label = typography.caption.copy(color = color),
        )

    @JvmStatic
    @CheckResult
    fun specialPrice(typography: Typography, color: Color) =
        TickerSizes(
            title =
                typography.body1.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
            description =
                typography.caption.copy(
                    color = color,
                    fontWeight = FontWeight.W100,
                ),
            label =
                typography.caption.copy(
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W100,
                ),
        )
  }
}
