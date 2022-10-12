package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

enum class TickerSize(val isSpecial: Boolean) {
  CHART(isSpecial = false),

  // Quotes
  QUOTE(isSpecial = false),
  QUOTE_EXTRA(isSpecial = true),

  // Recommendations
  RECOMMEND_QUOTE(isSpecial = false),
  RECOMMEND_QUOTE_EXTRA(isSpecial = true)
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
        alphaHigh: Float,
        alphaMedium: Float,
    ) =
        TickerSizes(
            title =
                typography.h6.copy(
                    color = color.copy(alpha = alphaHigh),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body2.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun quote(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) =
        TickerSizes(
            title =
                typography.h5.copy(
                    color = color.copy(alpha = alphaHigh),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.body1.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.caption.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
        )

    @JvmStatic
    @CheckResult
    fun price(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) = quote(typography, color, alphaHigh, alphaMedium)

    @JvmStatic
    @CheckResult
    fun priceExtra(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) = chart(typography, color, alphaHigh, alphaMedium)

    @JvmStatic
    @CheckResult
    fun recPrice(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) = priceExtra(typography, color, alphaHigh, alphaMedium)

    @JvmStatic
    @CheckResult
    fun recPriceExtra(
        typography: Typography,
        color: Color,
        alphaHigh: Float,
        alphaMedium: Float,
    ) =
        TickerSizes(
            title =
                typography.body1.copy(
                    color = color.copy(alpha = alphaHigh),
                    fontWeight = FontWeight.W700,
                ),
            description =
                typography.caption.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
            label =
                typography.overline.copy(
                    color = color.copy(alpha = alphaMedium),
                    fontWeight = FontWeight.W400,
                ),
        )
  }
}
