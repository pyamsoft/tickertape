package com.pyamsoft.tickertape.core

import androidx.annotation.CheckResult
import java.time.LocalDate

/**
 * Cannot use Duration.between(start, end) which internally attempts to convert to seconds since
 * LocalDate doesn't work with seconds
 *
 * Internally though, it calls this, so just do this directly
 */
@CheckResult
private fun LocalDate.getDaysSincePurchase(time: LocalDate): Long {
  return time.toEpochDay() - this.toEpochDay()
}

@CheckResult
fun LocalDate.isShortTermPurchase(time: LocalDate): Boolean {
  return this.getDaysSincePurchase(time) < 365
}

@CheckResult
fun LocalDate.isLongTermPurchase(time: LocalDate): Boolean {
  return this.getDaysSincePurchase(time) >= 365
}
