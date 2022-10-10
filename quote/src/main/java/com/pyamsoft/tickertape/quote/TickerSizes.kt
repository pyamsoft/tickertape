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
    fun chart(
        typography: Typography,
        color: Color,
    ) =
        TickerSizes(
            title =
                typography.h6.copy(
                    color = color,
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body2.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun quote(
        typography: Typography,
        color: Color,
    ) =
        TickerSizes(
            title =
                typography.h5.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body1.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun price(
        typography: Typography,
        color: Color,
    ) =
        TickerSizes(
            title =
                typography.h4.copy(
                    color = color,
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.h6.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun specialPrice(
        typography: Typography,
        color: Color,
    ) =
        TickerSizes(
            title =
                typography.h6.copy(
                    color = color,
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body2.copy(
                    color = color,
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W400,
                ),
        )
  }
}
