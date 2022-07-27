package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.StockQuote

private val UP_COLOR_10 = Color(0xFF2E7D32)
private val UP_COLOR_7 = Color(0xFF388E3C)
private val UP_COLOR_5 = Color(0xFF4CAF50)
private val UP_COLOR_2 = Color(0xFF8BC34A)
private val UP_COLOR_1 = Color(0xFFAED581)

private val DOWN_COLOR_10 = Color(0xFFC62828)
private val DOWN_COLOR_7 = Color(0xFFD32F2F)
private val DOWN_COLOR_5 = Color(0xFFE53935)
private val DOWN_COLOR_2 = Color(0xFFF44336)
private val DOWN_COLOR_1 = Color(0xFFE57373)

@CheckResult
private fun decideCardBackgroundColorForPercentChange(
    percentChange: Double,
    defaultColor: Color,
): Color {
  return when {
    percentChange.isZero() -> defaultColor
    percentChange >= 10 -> UP_COLOR_10
    percentChange >= 7 -> UP_COLOR_7
    percentChange >= 5 -> UP_COLOR_5
    percentChange >= 2 -> UP_COLOR_2
    percentChange >= 1 -> UP_COLOR_1
    percentChange <= -10 -> DOWN_COLOR_10
    percentChange <= -7 -> DOWN_COLOR_7
    percentChange <= -5 -> DOWN_COLOR_5
    percentChange <= -2 -> DOWN_COLOR_2
    percentChange <= -1 -> DOWN_COLOR_1
    else -> defaultColor
  }
}

@CheckResult
private fun decideCardContentColorForPercentChange(
    colors: Colors,
    percentChange: Double,
): Color {
  val defaultColor = colors.onSurface

  return when {
    percentChange.isZero() -> defaultColor
    percentChange >= 10 -> colors.onSurface
    percentChange >= 7 -> colors.onSurface
    percentChange >= 5 -> colors.onSurface
    percentChange >= 2 -> colors.surface
    percentChange >= 1 -> colors.surface
    percentChange <= -10 -> colors.onSurface
    percentChange <= -7 -> colors.onSurface
    percentChange <= -5 -> colors.onSurface
    percentChange <= -2 -> colors.surface
    percentChange <= -1 -> colors.surface
    else -> defaultColor
  }
}

@Composable
@CheckResult
fun rememberCardBackgroundColorForQuote(
    quote: StockQuote?,
    defaultColor: Color = MaterialTheme.colors.secondary,
): Color {
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
    defaultColor: Color = MaterialTheme.colors.secondary,
): Color {
  return remember(percentChange, defaultColor) {
    decideCardBackgroundColorForPercentChange(percentChange, defaultColor)
  }
}

@Composable
@CheckResult
fun rememberCardContentColorForQuote(
    quote: StockQuote?,
): Color {
  val colors = MaterialTheme.colors
  if (quote == null) {
    return colors.onSurface
  }

  return remember(quote, colors) {
    val session = quote.currentSession
    val percentChange = session.percent.value
    return@remember decideCardContentColorForPercentChange(colors, percentChange)
  }
}

@Composable
@CheckResult
fun rememberCardContentColorForPercentChange(
    percentChange: Double,
): Color {
  val colors = MaterialTheme.colors
  return remember(percentChange, colors) {
    decideCardContentColorForPercentChange(colors, percentChange)
  }
}
