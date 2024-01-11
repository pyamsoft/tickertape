/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
