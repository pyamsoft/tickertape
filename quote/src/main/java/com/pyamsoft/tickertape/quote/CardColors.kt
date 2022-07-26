package com.pyamsoft.tickertape.quote

import androidx.annotation.CheckResult
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.StockQuote

private val UP_COLOR_10 = Color.Green
private val UP_COLOR_7 = UP_COLOR_10.copy(alpha = 0.7F)
private val UP_COLOR_5 = UP_COLOR_10.copy(alpha = 0.5F)
private val UP_COLOR_2 = UP_COLOR_10.copy(alpha = 0.2F)
private val UP_COLOR_1 = UP_COLOR_10.copy(alpha = 0.1F)

private val DOWN_COLOR_10 = Color.Red
private val DOWN_COLOR_7 = DOWN_COLOR_10.copy(alpha = 0.7F)
private val DOWN_COLOR_5 = DOWN_COLOR_10.copy(alpha = 0.5F)
private val DOWN_COLOR_2 = DOWN_COLOR_10.copy(alpha = 0.2F)
private val DOWN_COLOR_1 = DOWN_COLOR_10.copy(alpha = 0.1F)

@CheckResult
private fun decideCardColorForPercentChange(
    percentChange: Double,
    defaultColor: Color,
): Color {
  return when {
    percentChange.isZero() -> defaultColor
    percentChange >= 1 -> UP_COLOR_1
    percentChange >= 2 -> UP_COLOR_2
    percentChange >= 5 -> UP_COLOR_5
    percentChange >= 7 -> UP_COLOR_7
    percentChange >= 10 -> UP_COLOR_10
    percentChange <= -1 -> DOWN_COLOR_1
    percentChange <= -2 -> DOWN_COLOR_2
    percentChange <= -5 -> DOWN_COLOR_5
    percentChange <= -7 -> DOWN_COLOR_7
    percentChange <= -10 -> DOWN_COLOR_10
    else -> defaultColor
  }
}

@Composable
@CheckResult
fun rememberCardColorForQuote(
    quote: StockQuote?,
    defaultColor: Color = MaterialTheme.colors.secondary,
): Color {
  if (quote == null) {
    return defaultColor
  }

  return remember(quote) {
    val session = quote.currentSession
    val percentChange = session.percent.value
    return@remember decideCardColorForPercentChange(percentChange, defaultColor)
  }
}

@Composable
@CheckResult
fun rememberCardColorForPercentChange(
    percentChange: Double,
    defaultColor: Color = MaterialTheme.colors.secondary,
): Color {
  return remember(percentChange) { decideCardColorForPercentChange(percentChange, defaultColor) }
}
