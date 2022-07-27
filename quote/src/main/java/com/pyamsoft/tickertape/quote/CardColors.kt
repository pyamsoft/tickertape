package com.pyamsoft.tickertape.quote

import android.graphics.Color
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.pyamsoft.tickertape.core.isNegative
import com.pyamsoft.tickertape.core.isPositive
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.StockQuote

private const val LIMIT_PERCENT = 10
private val UP_COLOR = Color.parseColor("4CAF50")
private val DOWN_COLOR = Color.parseColor("E53935")

val QUOTE_BACKGROUND_DEFAULT_COLOR = ComposeColor(0xFF709EFF)
val QUOTE_CONTENT_DEFAULT_COLOR = ComposeColor(0xFF121212)
const val QUOTE_CONTENT_DEFAULT_ALPHA = 0.6F

@CheckResult
private fun decideCardBackgroundColorForPercentChange(
    percentChange: Double,
    defaultColor: ComposeColor,
): ComposeColor {
  return when {
    percentChange.isZero() -> defaultColor
    percentChange.isPositive() -> {
      val diff = ((LIMIT_PERCENT - percentChange) / 100.0).toFloat()
      ComposeColor(ColorUtils.blendARGB(defaultColor.toArgb(), UP_COLOR, diff))
    }
    percentChange.isNegative() -> {
      val diff = ((LIMIT_PERCENT - percentChange) / 100.0).toFloat()
      ComposeColor(ColorUtils.blendARGB(defaultColor.toArgb(), DOWN_COLOR, diff))
    }
    else -> defaultColor
  }
}

@Composable
@CheckResult
fun rememberCardBackgroundColorForQuote(
    quote: StockQuote?,
    defaultColor: ComposeColor = QUOTE_BACKGROUND_DEFAULT_COLOR,
): ComposeColor {
  if (quote == null) {
    return defaultColor
  }

  return remember(quote, defaultColor) {
    val session = quote.currentSession
    val percentChange = session.percent.value
    return@remember decideCardBackgroundColorForPercentChange(percentChange, defaultColor)
  }
}

@Composable
@CheckResult
fun rememberCardBackgroundColorForPercentChange(
    percentChange: Double,
    defaultColor: ComposeColor = QUOTE_BACKGROUND_DEFAULT_COLOR,
): ComposeColor {
  return remember(percentChange, defaultColor) {
    decideCardBackgroundColorForPercentChange(percentChange, defaultColor)
  }
}
