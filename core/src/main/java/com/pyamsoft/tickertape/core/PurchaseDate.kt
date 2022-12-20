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
private fun LocalDate.getDaysSincePurchase(now: LocalDate): Long {
    return now.toEpochDay() - this.toEpochDay()
}

@CheckResult
@JvmOverloads
fun LocalDate.isShortTermPurchase(
    now: LocalDate = LocalDate.now(),
): Boolean {
    return this.getDaysSincePurchase(now) < 365
}

@CheckResult
@JvmOverloads
fun LocalDate.isLongTermPurchase(
    now: LocalDate = LocalDate.now(),
): Boolean {
    return this.getDaysSincePurchase(now) >= 365
}
