package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

enum class TickerSize {
  CHART,
  QUOTE
}

data class TickerSizes
internal constructor(
    val title: TextStyle,
    val description: TextStyle,
) {

  companion object {
    @JvmStatic
    @CheckResult
    fun chart(typography: Typography, color: Color) =
        TickerSizes(
            title = typography.body1.copy(color = color),
            description = typography.body2.copy(color = color),
        )

    @JvmStatic
    @CheckResult
    fun company(typography: Typography, color: Color): TickerSizes {
      val titleStyle = typography.h6
      val descriptionStyle = typography.body2
      return TickerSizes(
          title =
              titleStyle.copy(
                  color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
              ),
          description =
              descriptionStyle.copy(
                  color = color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
              ),
      )
    }

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
        )
  }
}
