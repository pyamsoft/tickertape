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
    private val existingPositionId: DbPosition.Id,
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

  private fun handleDateChanged(dateOfPurchase: LocalDate) {
    state.dateOfPurchase = dateOfPurchase
    checkSubmittable()
  }

  @CheckResult
  private fun resolvePosition(existing: DbPosition?): DbPosition {
    val s = state
    val price = s.pricePerShare.toDouble()
    val shareCount = s.numberOfShares.toDouble()
    val date = s.dateOfPurchase.requireNotNull()

    return if (existing == null) {
      Timber.d("No existing position, make new one for holding: $holdingId")
      JsonMappableDbPosition.create(
              holdingId = holdingId,
              shareCount = shareCount.asShares(),
              price = price.asMoney(),
              purchaseDate = date.atTime(0, 0),
          )
          .also { Timber.d("Created new position: $it") }
    } else {
      existing
          .price(price.asMoney())
          .shareCount(shareCount.asShares())
          .purchaseDate(date.atTime(0, 0))
          .also { Timber.d("Update existing position: $it") }
    }
  }

  private fun loadExistingPositionData(existing: DbPosition) {
    Timber.d("Load existing position data from position: $existing")
    state.apply {
      // Save this full position for later
      existingPosition = existing

      // Make sure this number is good (it should be, but just to be safe)
      existing.price().value().toString().also { p ->
        if (isSafe(p)) {
          pricePerShare = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // Make sure this number is good (it should be, but just to be safe)
      existing.shareCount().value().toString().also { p ->
        if (isSafe(p)) {
          numberOfShares = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // After date, check submittable
      dateOfPurchase = existing.purchaseDate().toLocalDate()
      checkSubmittable()
    }
  }

  fun bind(scope: CoroutineScope) {
    // Only if this ID is not EMPTY
    existingPositionId.also { existingId ->
      if (!existingId.isEmpty()) {
        Timber.d("Existing position ID, attempt load: $existingId")
        scope.launch(context = Dispatchers.Main) {
          interactor
              .loadExistingPosition(existingId)
              .onFailure { Timber.e(it, "Failed to load existing position") }
              .onSuccess { Timber.d("Existing position loaded: $it") }
              .onSuccess { loadExistingPositionData(it) }
        }
      }
    }

    scope.launch(context = Dispatchers.Main) {
      datePickerEventBus.onEvent { e ->
        if (e.positionId == state.positionId) {
          val dateOfPurchase = LocalDate.of(e.year, e.month, e.dayOfMonth)
          handleDateChanged(dateOfPurchase)
        }
      }
    }
  }

  fun handleSubmit(
      scope: CoroutineScope,
      onClose: () -> Unit,
  ) {
    val s = state
    s.apply {
      if (isSubmitting || !isSubmittable) {
        return
      }
    }

    s.isSubmitting = true
    scope.launch(context = Dispatchers.Main) {
      val position = resolvePosition(s.existingPosition)
      interactor
          .submitPosition(position)
          .onFailure { Timber.e(it, "Error when submitting position: $position") }
          .onSuccess { result ->
            when (result) {
              is DbInsert.InsertResult.Insert -> Timber.d("Position was inserted: ${result.data}")
              is DbInsert.InsertResult.Update ->
                  Timber.d("Position was updated: ${result.data} from ${s.existingPosition}")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to submit new position: $position")
                // Caught by the onFailure below
                throw result.error
              }
            }
          }
          .onSuccess {
            s.apply {
              dateOfPurchase = null
              numberOfShares = ""
              pricePerShare = ""

              if (existingPosition == null) {
                Timber.d("New position created, prep for another entry")
                newPosition()
              } else {
                Timber.d("Existing position updated, close screen: $existingPosition")
                onClose()
              }
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
