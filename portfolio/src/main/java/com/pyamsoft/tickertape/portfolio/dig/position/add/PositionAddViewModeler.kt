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
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.holding.DbHolding
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class PositionAddViewModeler
@Inject
internal constructor(
    private val state: MutablePositionAddViewState,
    private val datePickerEventBus: EventConsumer<DatePickerEvent>,
    private val holdingId: DbHolding.Id,
) : AbstractViewModeler<PositionAddViewState>(state) {

  private fun checkSubmittable() {
    state.apply {
      val isAllValuesDefined =
          pricePerShare.toDoubleOrNull() != null &&
              numberOfShares.toDoubleOrNull() != null &&
              dateOfPurchase != null
      isSubmittable = isAllValuesDefined
    }
  }

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) {
      datePickerEventBus.onEvent { e ->
        if (e.positionId == state.positionId) {
          val dateOfPurchase = LocalDate.of(e.year, e.month, e.dayOfMonth)
          handleDateChanged(dateOfPurchase)
        }
      }
    }
  }

  fun handleSubmit(scope: CoroutineScope) {
    val s = state
    s.apply {
      if (isSubmitting || !isSubmittable) {
        return
      }
    }

    s.isSubmitting = true
    scope.launch(context = Dispatchers.Main) {
      val price = s.pricePerShare.toDouble()
      val shareCount = s.numberOfShares.toDouble()
      val date = s.dateOfPurchase.requireNotNull()

      // TODO
      Timber.d("Submit new position: ", s.positionId, holdingId, price, shareCount, date)
      delay(1000)

      s.isSubmitting = false
    }
  }

  fun handlePriceChanged(pricePerShare: String) {
    if (isSafe(pricePerShare)) {
      state.pricePerShare = pricePerShare
      checkSubmittable()
    }
  }

  fun handleNumberChanged(numberOfShares: String) {
    if (isSafe(numberOfShares)) {
      state.numberOfShares = numberOfShares
      checkSubmittable()
    }
  }

  private fun handleDateChanged(dateOfPurchase: LocalDate) {
    state.dateOfPurchase = dateOfPurchase
    checkSubmittable()
  }

  companion object {

    private val WHITESPACE_REGEX = Regex("\\s+")

    @JvmStatic
    @CheckResult
    private fun isSafe(numberString: String): Boolean {
      // Only one decimal is valid
      if (numberString.count { it == '.' } > 1) {
        return false
      }

      // No whitespace
      if (numberString.contains(WHITESPACE_REGEX)) {
        return false
      }

      // No negative sign
      if (numberString.contains('-')) {
        return false
      }

      // No commas
      if (numberString.contains(',')) {
        return false
      }

      // Finally, attempt a cast
      return numberString.toDoubleOrNull() != null
    }
  }
}
