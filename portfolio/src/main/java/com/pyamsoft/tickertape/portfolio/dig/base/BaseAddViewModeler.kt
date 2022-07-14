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

package com.pyamsoft.tickertape.portfolio.dig.base

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.db.IdType
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseAddViewModeler<S : UiViewState, Id : IdType>
protected constructor(
    private val datePickerEventBus: EventConsumer<DateSelectedEvent<Id>>,
    private val existingId: Id,
    state: S,
) : AbstractViewModeler<S>(state) {

  private fun handleDateChanged(date: LocalDate) {
    onDateChanged(date)
    checkSubmittable()
  }

  protected fun onBind(
      scope: CoroutineScope,
      onHasExistingId: (Id) -> Unit,
  ) {
    scope.launch(context = Dispatchers.Main) {
      datePickerEventBus.onEvent { e ->
        if (e.id == existingId) {
          val dateOfPurchase = LocalDate.of(e.year, e.month, e.dayOfMonth)
          handleDateChanged(dateOfPurchase)
        }
      }
    }

    existingId.also { eid ->
      if (!eid.isEmpty()) {
        Timber.d("Existing ID, attempt load: $eid")
        onHasExistingId(eid)
      }
    }
  }

  protected fun handleNumberAsStringChange(numberAsString: String, onChange: (String) -> Unit) {
    if (isSafe(numberAsString)) {
      onChange(numberAsString)
      checkSubmittable()
    }
  }

  protected abstract fun checkSubmittable()

  protected abstract fun onDateChanged(date: LocalDate)

  abstract fun handleSubmit(
      scope: CoroutineScope,
      onClose: () -> Unit,
  )

  companion object {

    private val WHITESPACE_REGEX = Regex("\\s+")

    @JvmStatic
    @CheckResult
    protected fun isSafe(numberString: String): Boolean {
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
