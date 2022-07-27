package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

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
    fun company(typography: Typography): TickerSizes {
      val titleStyle = typography.h6
      val descriptionStyle = typography.body1
      return TickerSizes(
          title =
              titleStyle.copy(
                  color = titleStyle.color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
              ),
          description =
              descriptionStyle.copy(
                  color = descriptionStyle.color.copy(alpha = QUOTE_CONTENT_DEFAULT_ALPHA),
              ),
      )
    }

    @JvmStatic
    @CheckResult
    fun price(typography: Typography) =
        TickerSizes(
            title = typography.h5.copy(fontWeight = FontWeight.W700),
            description = typography.body2.copy(fontWeight = FontWeight.W600),
        )
  }
}
