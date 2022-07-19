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
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.JsonMappableDbSplit
import com.pyamsoft.tickertape.portfolio.dig.base.BaseAddViewModeler
import com.pyamsoft.tickertape.portfolio.dig.base.DateSelectedEvent
import com.pyamsoft.tickertape.stocks.api.asShares
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SplitAddViewModeler
@Inject
internal constructor(
    private val state: MutableSplitAddViewState,
    private val holdingId: DbHolding.Id,
    private val interactor: SplitAddInteractor,
    datePickerEventBus: EventConsumer<DateSelectedEvent<DbSplit.Id>>,
    existingSplitId: DbSplit.Id,
) :
    BaseAddViewModeler<SplitAddViewState, DbSplit.Id>(
        state = state,
        datePickerEventBus = datePickerEventBus,
        existingId = existingSplitId,
    ) {

  @CheckResult
  private fun resolveSplit(existing: DbSplit?): DbSplit {
    val s = state
    val preSplit = s.preSplitShareCount.toDouble()
    val postSplit = s.postSplitShareCount.toDouble()
    val date = s.splitDate.requireNotNull()

    return if (existing == null) {
      Timber.d("No existing split, make new one for holding: ${s.splitId} $holdingId")
      JsonMappableDbSplit.create(
              // Use the splitId here, this will be changed if newPosition() is called
              id = s.splitId,
              holdingId = holdingId,
              preSplitShareCount = preSplit.asShares(),
              postSplitShareCount = postSplit.asShares(),
              splitDate = date.atTime(0, 0),
          )
          .also { Timber.d("Created new split: $it") }
    } else {
      existing
          .preSplitShareCount(preSplit.asShares())
          .postSplitShareCount(postSplit.asShares())
          .splitDate(date.atTime(0, 0))
          .also { Timber.d("Update existing split: $it") }
    }
  }

  private fun loadExistingSplitData(existing: DbSplit) {
    Timber.d("Load existing split data from split: $existing")
    state.apply {
      // Save this full position for later
      existingSplit = existing

      // Make sure this number is good (it should be, but just to be safe)
      existing.preSplitShareCount().value.toString().also { p ->
        if (isSafe(p)) {
          preSplitShareCount = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // Make sure this number is good (it should be, but just to be safe)
      existing.postSplitShareCount().value.toString().also { p ->
        if (isSafe(p)) {
          postSplitShareCount = p
          // We always check at the end
          //
          // checkSubmittable()
        }
      }

      // After date, check submittable
      splitDate = existing.splitDate().toLocalDate()
      checkSubmittable()
    }
  }

  override fun onDateChanged(date: LocalDate) {
    state.splitDate = date
  }

  override fun checkSubmittable() {
    state.apply {
      val isAllValuesDefined =
          preSplitShareCount.toDoubleOrNull() != null &&
              postSplitShareCount.toDoubleOrNull() != null &&
              splitDate != null
      isSubmittable = isAllValuesDefined
    }
  }

  override fun isCurrentId(id: DbSplit.Id): Boolean {
    return id == state.splitId
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
      val split = resolveSplit(s.existingSplit)
      interactor
          .submitSplit(split)
          .onFailure { Timber.e(it, "Error when submitting split: $split") }
          .onSuccess { result ->
            when (result) {
              is DbInsert.InsertResult.Insert -> Timber.d("Split was inserted: ${result.data}")
              is DbInsert.InsertResult.Update ->
                  Timber.d("Split was updated: ${result.data} from ${s.existingSplit}")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to submit split: $split")
                // Caught by the onFailure below
                throw result.error
              }
            }
          }
          .onSuccess {
            s.apply {
              splitDate = null
              preSplitShareCount = ""
              postSplitShareCount = ""

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
            // TODO handle split add error
          }
          .onFinally { s.isSubmitting = false }
    }
  }

  fun bind(scope: CoroutineScope) {
    onBind(scope) { existingId ->
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
    handleNumberAsStringChange(preSplitCount) { state.preSplitShareCount = it }
  }

  fun handlePostSplitShareCountChanged(postSplitCount: String) {
    handleNumberAsStringChange(postSplitCount) { state.postSplitShareCount = it }
  }
}
