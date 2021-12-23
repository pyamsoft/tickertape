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
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.JsonMappableDbPosition
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PositionAddViewModeler
@Inject
internal constructor(
    private val state: MutablePositionAddViewState,
    private val datePickerEventBus: EventConsumer<DatePickerEvent>,
    private val holdingId: DbHolding.Id,
    private val interactor: PositionAddInteractor,
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
    scope.launch(context = Dispatchers.Main) {
      datePickerEventBus.onEvent { e ->
        if (e.positionId == state.positionId) {
          val dateOfPurchase = LocalDate.of(e.year, e.month, e.dayOfMonth)
          handleDateChanged(dateOfPurchase)
        }
      }
    }
  }

  @CheckResult
  private fun createPosition(): DbPosition {
    val s = state
    val price = s.pricePerShare.toDouble()
    val shareCount = s.numberOfShares.toDouble()
    val date = s.dateOfPurchase.requireNotNull()

    return JsonMappableDbPosition.create(
        holdingId = holdingId,
        shareCount = shareCount.asShares(),
        price = price.asMoney(),
        purchaseDate = date.atTime(0, 0))
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
      val position = createPosition()
      interactor
          .addNewPosition(position)
          .onFailure { Timber.e(it, "Error when adding position: $position") }
          .onSuccess { result ->
            when (result) {
              is DbInsert.InsertResult.Insert -> Timber.d("Position was inserted: ${result.data}")
              // NOTE(Peter): Do we throw an error? I guess it's harmless
              is DbInsert.InsertResult.Update ->
                  Timber.w("Position was updated but should not exist previously! ${result.data}")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to insert new position: $position")
                // Caught by the onFailure below
                throw result.error
              }
            }
          }
          .onSuccess {
            s.apply {
              newPosition()
              dateOfPurchase = null
              numberOfShares = ""
              pricePerShare = ""
            }
          }
          .onFailure {
            // TODO handle position add error
          }
          .onFinally { s.isSubmitting = false }
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
