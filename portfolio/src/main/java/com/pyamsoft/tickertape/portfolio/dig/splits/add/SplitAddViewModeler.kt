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

package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.JsonMappableDbSplit
import com.pyamsoft.tickertape.portfolio.dig.base.BaseAddViewModeler
import com.pyamsoft.tickertape.quote.dig.SplitParams
import com.pyamsoft.tickertape.stocks.api.asShares
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SplitAddViewModeler
@Inject
internal constructor(
    override val state: MutableSplitAddViewState,
    private val interactor: SplitAddInteractor,
    private val params: SplitParams,
    private val clock: Clock,
) :
    BaseAddViewModeler<SplitAddViewState, DbSplit.Id>(
        state = state,
        existingId = params.existingSplitId,
    ) {

  private var existingSplit: DbSplit? = null
  private var splitId = decideInitialSplitId(params.existingSplitId)

  private fun newSplit() {
    val existing = params.existingSplitId
    if (existing.isEmpty) {
      splitId = generateNewSplitId()
    } else {
      throw IllegalStateException("Do not use newPosition() with existing ID: $existing")
    }
  }

  @CheckResult
  private fun resolveSplit(existing: DbSplit?): DbSplit {
    val s = state
    val preSplit = s.preSplitShareCount.value.toDouble()
    val postSplit = s.postSplitShareCount.value.toDouble()
    val date = s.splitDate.value.requireNotNull()

    val holdingId = params.holdingId

    return if (existing == null) {
      Timber.d("No existing split, make new one for holding: $splitId $holdingId")
      JsonMappableDbSplit.create(
              // Use the splitId here, this will be changed if newPosition() is called
              id = splitId,
              holdingId = holdingId,
              preSplitShareCount = preSplit.asShares(),
              postSplitShareCount = postSplit.asShares(),
              splitDate = date,
          )
          .also { Timber.d("Created new split: $it") }
    } else {
      existing
          .preSplitShareCount(preSplit.asShares())
          .postSplitShareCount(postSplit.asShares())
          .splitDate(date)
          .also { Timber.d("Update existing split: $it") }
    }
  }

  private fun loadExistingSplitData(existing: DbSplit) {
    Timber.d("Load existing split data from split: $existing")
    state.apply {
      // Save this full position for later
      existingSplit = existing

      // Make sure this number is good (it should be, but just to be safe)
      existing.preSplitShareCount.value.toString().also { p ->
        if (isSafe(p)) {
          preSplitShareCount.value = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // Make sure this number is good (it should be, but just to be safe)
      existing.postSplitShareCount.value.toString().also { p ->
        if (isSafe(p)) {
          postSplitShareCount.value = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // After date, check submittable
      splitDate.value = existing.splitDate
      checkSubmittable()
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

  override fun onDateChanged(date: LocalDate) {
    state.splitDate.value = date
  }

  override fun checkSubmittable() {
    state.apply {
      val isAllValuesDefined =
          preSplitShareCount.value.toDoubleOrNull() != null &&
              postSplitShareCount.value.toDoubleOrNull() != null &&
              splitDate.value != null
      isSubmittable.value = isAllValuesDefined
    }
  }

  override fun isCurrentId(id: DbSplit.Id): Boolean {
    return id == splitId
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
      val split = resolveSplit(existingSplit)
      interactor
          .submitSplit(split)
          .onFailure { Timber.e(it, "Error when submitting split: $split") }
          .onSuccess { result ->
            when (result) {
              is DbInsert.InsertResult.Insert -> Timber.d("Split was inserted: ${result.data}")
              is DbInsert.InsertResult.Update ->
                  Timber.d("Split was updated: ${result.data} from $existingSplit")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to submit split: $split")
                // Caught by the onFailure below
                throw result.error
              }
            }
          }
          .onSuccess {
            s.apply {
              splitDate.value = null
              preSplitShareCount.value = ""
              postSplitShareCount.value = ""

              if (existingSplit == null) {
                Timber.d("New split created, prep for another entry")
                newSplit()
              } else {
                Timber.d("Existing split updated, close screen: $existingSplit")
                onClose()
              }
            }
          }
          .onFailure {
            Timber.e(it, "Error adding new split")
            // TODO handle split add error
          }
          .onFinally { s.isSubmitting.value = false }
    }
  }

  fun bind(scope: CoroutineScope) {
    onBind { existingId ->
      scope.launch(context = Dispatchers.Main) {
        interactor
            .loadExistingSplit(existingId)
            .onFailure { Timber.e(it, "Failed to load existing split") }
            .onSuccess { Timber.d("Existing split loaded: $it") }
            .onSuccess { loadExistingSplitData(it) }
      }
    }
  }

  fun handlePreSplitShareCountChanged(preSplitCount: String) {
    handleNumberAsStringChange(preSplitCount) { state.preSplitShareCount.value = it }
  }

  fun handlePostSplitShareCountChanged(postSplitCount: String) {
    handleNumberAsStringChange(postSplitCount) { state.postSplitShareCount.value = it }
  }

  fun handleOpenDateDialog(date: LocalDate?) {
    state.datePicker.value = date ?: LocalDate.now(clock)
  }

  fun handleCloseDateDialog() {
    state.datePicker.value = null
  }

  companion object {

    private const val KEY_DATE_PICKER = "key_date_picker"

    @JvmStatic
    @CheckResult
    private fun decideInitialSplitId(existingSplitId: DbSplit.Id): DbSplit.Id {
      return if (existingSplitId.isEmpty) {
        generateNewSplitId().also { Timber.d("Initial split ID created: $it") }
      } else {
        existingSplitId.also { Timber.d("Initial existing split ID: $it") }
      }
    }

    @JvmStatic
    @CheckResult
    private fun generateNewSplitId(): DbSplit.Id {
      return DbSplit.Id(IdGenerator.generate())
    }
  }
}
