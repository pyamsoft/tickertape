package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_DOWN_COLOR
import com.pyamsoft.tickertape.core.DEFAULT_STOCK_UP_COLOR
import com.pyamsoft.tickertape.core.isNegative
import com.pyamsoft.tickertape.core.isPositive
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.StockQuote
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Normal stocks am 8% move is kinda noticeable
const val QUOTE_DEFAULT_LIMIT_PERCENT = 8.0
// Options are a bit riskier, don't tell me unless this has moved 35%
const val OPTIONS_LIMIT_PERCENT = 35.0
// Crypto is wacky
const val CRYPTO_LIMIT_PERCENT = 85.0

// More generous change limit since positions can be big in the red or green
const val POSITION_CHANGE_LIMIT_PERCENT = 25.0

private const val COLOR_ADJUST_OFFSET = 0.05F

private val UP_COMPOSE_COLOR = Color(DEFAULT_STOCK_UP_COLOR)
private val DOWN_COMPOSE_COLOR = Color(DEFAULT_STOCK_DOWN_COLOR)

val QUOTE_BACKGROUND_DEFAULT_COLOR = Color(0xFF709EFF)
val QUOTE_CONTENT_DEFAULT_COLOR = Color(0xFF121212)
const val QUOTE_CONTENT_DEFAULT_ALPHA = 0.6F

@CheckResult
private fun decideCardBackgroundColorForPercentChange(
    percentChange: Double,
    limit: Double,
    defaultColor: Color,
): Color {
  if (limit.compareTo(0) <= 0) {
    throw IllegalArgumentException("Limit for color percent bound cannot be <= 0")
  }

  return when {
    percentChange.isZero() -> defaultColor
    percentChange.isPositive() -> {
      // If we are out of range, just feed directly UP_COLOR
      // blendARGB would also do this, but save some cycles
      if (percentChange.compareTo(10) >= 0) {
        UP_COMPOSE_COLOR
      } else {
        val diffPct = (percentChange / limit).toFloat()

        // Add small offset to bias a card towards the direction color
        val colorAmount = min(1F, max(0F, diffPct + COLOR_ADJUST_OFFSET))
        if (colorAmount.isZero()) {
          // If we are nothing, short circuit
          defaultColor
        } else if (colorAmount == 1F) {
          // If with adjustment we are 100%, short circuit
          UP_COMPOSE_COLOR
        } else {
          Color(ColorUtils.blendARGB(defaultColor.toArgb(), DEFAULT_STOCK_UP_COLOR, colorAmount))
        }
      }
    }
    percentChange.isNegative() -> {
      // If we are out of range, just feed directly DOWN_COLOR
      // blendARGB would also do this, but save some cycles
      if (percentChange.compareTo(-10) <= 0) {
        DOWN_COMPOSE_COLOR
      } else {
        val diffPct = abs(percentChange / limit).toFloat()

        // Add small offset to bias a card towards the direction color
        val colorAmount = min(1F, max(0F, diffPct + COLOR_ADJUST_OFFSET))
        if (colorAmount.isZero()) {
          // If we are nothing, short circuit
          defaultColor
        } else if (colorAmount == 1F) {
          // If with adjustment we are 100%, short circuit
          DOWN_COMPOSE_COLOR
        } else {
          Color(ColorUtils.blendARGB(defaultColor.toArgb(), DEFAULT_STOCK_DOWN_COLOR, colorAmount))
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
    changeLimit: Double = QUOTE_DEFAULT_LIMIT_PERCENT,
    defaultColor: Color = QUOTE_BACKGROUND_DEFAULT_COLOR,
): Color {
  if (quote == null) {
    return defaultColor
  }

  return remember(quote, changeLimit, defaultColor) {
    val session = quote.currentSession
    val percentChange = session.percent.value
    return@remember decideCardBackgroundColorForPercentChange(
        percentChange,
        changeLimit,
        defaultColor,
    )
  }
}

@Composable
@CheckResult
fun rememberCardBackgroundColorForPercentChange(
    percentChange: Double?,
    changeLimit: Double = QUOTE_DEFAULT_LIMIT_PERCENT,
    defaultColor: Color = QUOTE_BACKGROUND_DEFAULT_COLOR,
): Color {
  return remember(percentChange, changeLimit, defaultColor) {
    if (percentChange == null) {
      defaultColor
    } else {
      decideCardBackgroundColorForPercentChange(percentChange, changeLimit, defaultColor)
    }
  }
}
