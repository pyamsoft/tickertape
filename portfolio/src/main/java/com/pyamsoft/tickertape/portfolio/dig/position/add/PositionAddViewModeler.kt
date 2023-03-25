/*
 * Copyright 2023 pyamsoft
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
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.JsonMappableDbPosition
import com.pyamsoft.tickertape.portfolio.dig.base.BaseAddViewModeler
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PositionAddViewModeler
@Inject
internal constructor(
    override val state: MutablePositionAddViewState,
    private val interactor: PositionAddInteractor,
    private val params: PositionParams,
    private val clock: Clock,
) :
    BaseAddViewModeler<PositionAddViewState, DbPosition.Id>(
        state = state,
        existingId = params.existingPositionId,
    ) {

  private var existingPosition: DbPosition? = null
  private var positionId = decideInitialPositionId(params.existingPositionId)

  private fun newPosition() {
    val existing = params.existingPositionId
    if (existing.isEmpty) {
      positionId = generateNewPositionId()
    } else {
      throw IllegalStateException("Do not use newPosition() with existing ID: $existing")
    }
  }

  @CheckResult
  private fun resolvePosition(existing: DbPosition?): DbPosition {
    val s = state
    val price = s.pricePerShare.value.toDouble()
    val shareCount = s.numberOfShares.value.toDouble()
    val date = s.dateOfPurchase.value.requireNotNull()
    val holdingId = params.holdingId

    return if (existing == null) {
      Timber.d("No existing position, make new one for holding: $positionId $holdingId")
      JsonMappableDbPosition.create(
              // Use the positionId here, this will be changed if newPosition() is called
              id = positionId,
              holdingId = holdingId,
              shareCount = shareCount.asShares(),
              price = price.asMoney(),
              purchaseDate = date,
          )
          .also { Timber.d("Created new position: $it") }
    } else {
      existing.price(price.asMoney()).shareCount(shareCount.asShares()).purchaseDate(date).also {
        Timber.d("Update existing position: $it")
      }
    }
  }

  private fun loadExistingPositionData(existing: DbPosition) {
    Timber.d("Load existing position data from position: $existing")
    state.apply {
      // Save this full position for later
      existingPosition = existing

      // Make sure this number is good (it should be, but just to be safe)
      existing.price.value.toString().also { p ->
        if (isSafe(p)) {
          pricePerShare.value = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // Make sure this number is good (it should be, but just to be safe)
      existing.shareCount.value.toString().also { p ->
        if (isSafe(p)) {
          numberOfShares.value = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // After date, check submittable
      dateOfPurchase.value = existing.purchaseDate
      checkSubmittable()
    }
  }

  override fun checkSubmittable() {
    state.apply {
      val isAllValuesDefined =
          pricePerShare.value.toDoubleOrNull() != null &&
              numberOfShares.value.toDoubleOrNull() != null &&
              dateOfPurchase.value != null
      isSubmittable.value = isAllValuesDefined
    }
  }

  override fun onDateChanged(date: LocalDate) {
    state.dateOfPurchase.value = date
  }

  override fun isCurrentId(id: DbPosition.Id): Boolean {
    return id == positionId
  }

  override fun handleSubmit(
      scope: CoroutineScope,
      onClose: () -> Unit,
  ) {
    val s = state
    s.apply {
      if (isSubmitting.value || !isSubmittable.value) {
        return
      }
    }

    s.isSubmitting.value = true
    scope.launch(context = Dispatchers.Main) {
      val position = resolvePosition(existingPosition)
      interactor
          .submitPosition(position)
          .onFailure { Timber.e(it, "Error when submitting position: $position") }
          .onSuccess { result ->
            when (result) {
              is DbInsert.InsertResult.Insert -> Timber.d("Position was inserted: ${result.data}")
              is DbInsert.InsertResult.Update ->
                  Timber.d("Position was updated: ${result.data} from $existingPosition")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to submit position: $position")
                // Caught by the onFailure below
                throw result.error
              }
            }
          }
          .onSuccess {
            s.apply {
              dateOfPurchase.value = null
              numberOfShares.value = ""
              pricePerShare.value = ""

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
            Timber.e(it, "Failed to add new position")
            // TODO handle position add error
          }
          .onFinally { s.isSubmitting.value = false }
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        registry
            .registerProvider(KEY_DATE_PICKER) {
              state.datePicker.value?.format(DateTimeFormatter.ISO_DATE)
            }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_DATE_PICKER)
        ?.let { it as String }
        ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
        ?.also { handleOpenDateDialog(it) }
  }

  fun bind(scope: CoroutineScope) {
    onBind { existingId ->
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
    handleNumberAsStringChange(pricePerShare) { state.pricePerShare.value = it }
  }

  fun handleNumberChanged(numberOfShares: String) {
    handleNumberAsStringChange(numberOfShares) { state.numberOfShares.value = it }
  }

  fun handleOpenDateDialog(date: LocalDate?) {
    state.datePicker.value = date ?: LocalDate.now(clock)
  }

  fun handleCloseDateDialog() {
    state.datePicker.value = null
  }

  companion object {

    private const val KEY_DATE_PICKER = "key_date_picker"

    @CheckResult
    private fun decideInitialPositionId(existingPositionId: DbPosition.Id): DbPosition.Id {
      return if (existingPositionId.isEmpty) {
        generateNewPositionId().also { Timber.d("Initial position ID created: $it") }
      } else {
        existingPositionId.also { Timber.d("Initial existing position ID: $it") }
      }
    }

    @JvmStatic
    @CheckResult
    private fun generateNewPositionId(): DbPosition.Id {
      return DbPosition.Id(IdGenerator.generate())
    }
  }
}
