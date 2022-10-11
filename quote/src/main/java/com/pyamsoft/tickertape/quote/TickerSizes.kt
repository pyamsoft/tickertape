package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class TickerSize(val isSpecial: Boolean) {
  CHART(isSpecial = false),

  // Quotes
  QUOTE(isSpecial = false),
  QUOTE_EXTRA(isSpecial = true),

  // Recommendations
  RECOMMEND_QUOTE(isSpecial = false),
  RECOMMEND_QUOTE_EXTRA(isSpecial = true)
}

private const val EXTRA_CONTENT_DEFAULT_ALPHA = QUOTE_CONTENT_DEFAULT_ALPHA + 0.1F

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
                    color = color.copy(alpha = 1.0F),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body2.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
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
                    color = color.copy(alpha = 1.0F),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body1.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun price(
        typography: Typography,
        color: Color,
    ) = quote(typography, color)

    @JvmStatic
    @CheckResult
    fun priceExtra(
        typography: Typography,
        color: Color,
    ) = chart(typography, color)

    @JvmStatic
    @CheckResult
    fun recPrice(
        typography: Typography,
        color: Color,
    ) = priceExtra(typography, color)

    @JvmStatic
    @CheckResult
    fun recPriceExtra(
        typography: Typography,
        color: Color,
    ) =
        TickerSizes(
            title =
                typography.body1.copy(
                    color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.caption.copy(
                    color = color.copy(alpha = EXTRA_CONTENT_DEFAULT_ALPHA),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = EXTRA_CONTENT_DEFAULT_ALPHA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W400,
                ),
        )
  }
}
