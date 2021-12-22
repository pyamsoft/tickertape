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
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.holding.DbHolding
import java.time.LocalDateTime
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
    private val holdingId: DbHolding.Id,
) : AbstractViewModeler<PositionAddViewState>(state) {

  private fun checkSubmittable() {
    state.apply {
      val isAllValuesDefined =
          pricePerShare.toDoubleOrNull() != null && numberOfShares.toDoubleOrNull() != null && dateOfPurchase != null
      isSubmittable = isAllValuesDefined
    }
  }

  fun handleSubmit(scope: CoroutineScope) {
    state.apply {
      if (isSubmitting || !isSubmittable) {
        return
      }
    }

    state.isSubmitting = true
    scope.launch(context = Dispatchers.Main) {
      state.apply {
        val price = pricePerShare.toDouble()
        val shareCount = numberOfShares.toDouble()
        val date = dateOfPurchase.requireNotNull()
        Timber.d("Submit new position: ", price, shareCount, date)

        // TODO
        delay(1000)

        isSubmitting = false
      }
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

  fun handleDateChanged(dateOfPurchase: LocalDateTime) {
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
