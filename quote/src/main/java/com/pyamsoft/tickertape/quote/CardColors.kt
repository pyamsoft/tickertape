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
import kotlin.math.abs
import kotlin.math.max

private const val LIMIT_PERCENT = 10
private const val COLOR_ADJUST_OFFSET = 0.25F

private val UP_COLOR = Color.parseColor("#388E3C")
private val DOWN_COLOR = Color.parseColor("#D32F2F")

private val UP_COMPOSE_COLOR = ComposeColor(UP_COLOR)
private val DOWN_COMPOSE_COLOR = ComposeColor(DOWN_COLOR)

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
      // If we are out of range, just feed directly UP_COLOR
      // blendARGB would also do this, but save some cycles
      if (percentChange.compareTo(10) >= 0) {
        UP_COMPOSE_COLOR
      } else {
        val diff = LIMIT_PERCENT - percentChange
        val diffPct = (diff / LIMIT_PERCENT).toFloat()

        // Add small offset to bias a card towards the direction color
        val colorAmount = max(0F, diffPct - COLOR_ADJUST_OFFSET)
        if (colorAmount.isZero()) {
          // If with adjustment we are 100%, short circuit
          UP_COMPOSE_COLOR
        } else {
          ComposeColor(ColorUtils.blendARGB(UP_COLOR, defaultColor.toArgb(), colorAmount))
        }
      }
    }
    percentChange.isNegative() -> {
      // If we are out of range, just feed directly DOWN_COLOR
      // blendARGB would also do this, but save some cycles
      if (percentChange.compareTo(-10) <= 0) {
        DOWN_COMPOSE_COLOR
      } else {
        val diff = -(LIMIT_PERCENT) - percentChange
        val diffPct = abs(diff / LIMIT_PERCENT).toFloat()

        // Add small offset to bias a card towards the direction color
        val colorAmount = max(0F, diffPct - COLOR_ADJUST_OFFSET)
        if (colorAmount.isZero()) {
          // If with adjustment we are 100%, short circuit
          DOWN_COMPOSE_COLOR
        } else {
          ComposeColor(ColorUtils.blendARGB(DOWN_COLOR, defaultColor.toArgb(), colorAmount))
        }
      }
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
