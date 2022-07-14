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
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.JsonMappableDbPosition
import com.pyamsoft.tickertape.portfolio.dig.base.BaseAddViewModeler
import com.pyamsoft.tickertape.portfolio.dig.base.DateSelectedEvent
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
    private val holdingId: DbHolding.Id,
    private val interactor: PositionAddInteractor,
    datePickerEventBus: EventConsumer<DateSelectedEvent<DbPosition.Id>>,
    existingPositionId: DbPosition.Id,
) :
    BaseAddViewModeler<PositionAddViewState, DbPosition.Id>(
        state = state,
        datePickerEventBus = datePickerEventBus,
        existingId = existingPositionId,
    ) {

  @CheckResult
  private fun resolvePosition(existing: DbPosition?): DbPosition {
    val s = state
    val price = s.pricePerShare.toDouble()
    val shareCount = s.numberOfShares.toDouble()
    val date = s.dateOfPurchase.requireNotNull()

    return if (existing == null) {
      Timber.d("No existing position, make new one for holding: ${s.positionId} $holdingId")
      JsonMappableDbPosition.create(
              // Use the positionId here, this will be changed if newPosition() is called
              id = s.positionId,
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

  override fun checkSubmittable() {
    state.apply {
      val isAllValuesDefined =
          pricePerShare.toDoubleOrNull() != null &&
              numberOfShares.toDoubleOrNull() != null &&
              dateOfPurchase != null
      isSubmittable = isAllValuesDefined
    }
  }

  override fun onDateChanged(date: LocalDate) {
    state.dateOfPurchase = date
  }

  override fun isCurrentId(id: DbPosition.Id): Boolean {
    return id == state.positionId
  }

  override fun handleSubmit(
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
                Timber.e(result.error, "Failed to submit position: $position")
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

  fun bind(scope: CoroutineScope) {
    onBind(scope) { existingId ->
      scope.launch(context = Dispatchers.Main) {
        interactor
            .loadExistingPosition(existingId)
            .onFailure { Timber.e(it, "Failed to load existing position") }
            .onSuccess { Timber.d("Existing position loaded: $it") }
            .onSuccess { loadExistingPositionData(it) }
      }
    }
  }

  fun handlePriceChanged(pricePerShare: String) {
    handleNumberAsStringChange(pricePerShare) { state.pricePerShare = it }
  }

  fun handleNumberChanged(numberOfShares: String) {
    handleNumberAsStringChange(numberOfShares) { state.numberOfShares = it }
  }
}
