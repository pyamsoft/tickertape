/*
 * Copyright 2021 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tickertape.db.holding.DbHolding
import java.time.LocalDateTime
import javax.inject.Inject

class PositionAddViewModeler
@Inject
internal constructor(
    private val state: MutablePositionAddViewState,
    private val holdingId: DbHolding.Id,
) : AbstractViewModeler<PositionAddViewState>(state) {

  fun handlePriceChanged(pricePerShare: String) {
    state.pricePerShare = pricePerShare
  }

  fun handleNumberChanged(numberOfShares: String) {
    state.numberOfShares = numberOfShares
  }

  fun handleDateChanged(dateOfPurchase: LocalDateTime) {
    state.dateOfPurchase = dateOfPurchase
  }

  companion object {

    // Removes anything that isn't a number or the decimal point
    private val DECIMAL_NUMBER_REGEX = Regex("[^0-9.]")

    @JvmStatic
    @CheckResult
    private fun textToDecimalNumber(text: String): Double {
      return text.trim().replace(DECIMAL_NUMBER_REGEX, "").toDoubleOrNull() ?: 0.0
    }
  }
}
