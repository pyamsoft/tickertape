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

package com.pyamsoft.tickertape.portfolio.manage.position.add

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.UnitViewEvent
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsDateControllerEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsDateViewModel
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * We use this instead of MaterialDatePicker because it loses all of it's onClick listeners on
 * device rotation.
 */
internal class PurchaseDatePickerDialog : AppCompatDialogFragment(), UiController<PositionsDateControllerEvent> {

  @JvmField @Inject
  internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<PositionsDateViewModel>(activity = true) {
    factory?.create(requireActivity())
  }

  private var stateSaver: StateSaver? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.obtainFromApplication<TickerComponent>(requireActivity())
      .plusPositionDateComponent()
      .create()
      .inject(this)

    stateSaver =
      createComponent<UnitViewState, UnitViewEvent, PositionsDateControllerEvent>(
        savedInstanceState, this, viewModel, this) {}
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val listener =
      DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        // month is 0-11 but LDT requires 1-12
        viewModel.handleDateSelected(LocalDateTime.of(year, month + 1, dayOfMonth, 0 ,0 ))
      }

    val argYear = requireArguments().getInt(KEY_YEAR, -1)
    val argMonth = requireArguments().getInt(KEY_MONTH, -1)
    val argDay = requireArguments().getInt(KEY_DAY, -1)

    val year: Int
    val month: Int
    val day: Int
    if (argYear < 0 || argMonth < 0 || argDay < 0) {
      val now = LocalDateTime.now()
      year = now.year
      month = now.monthValue
      day = now.dayOfMonth
    } else {
      year = argYear
      month = argMonth
      day = argDay
    }

    // Month from an LDT is 1-12 but this requires 0-11
    return DatePickerDialog(requireActivity(), listener, year, month - 1, day)
      .apply { datePicker.maxDate = Instant.now().toEpochMilli() }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    stateSaver = null
    factory = null
  }

  override fun onControllerEvent(event: PositionsDateControllerEvent) {
      return when (event) {
        is PositionsDateControllerEvent.Close -> dismiss()
      }
  }

  companion object {

    const val TAG = "PurchaseDatePickerDialog"
    private const val KEY_YEAR = "key_year"
    private const val KEY_MONTH = "key_month"
    private const val KEY_DAY = "key_day"

    @JvmStatic
    @CheckResult
    fun newInstance(date: LocalDateTime?): DialogFragment {
      return PurchaseDatePickerDialog().apply {
        arguments =
            Bundle().apply {
          if (date != null) {
            putInt(KEY_YEAR, date.year)
            putInt(KEY_MONTH, date.monthValue)
            putInt(KEY_DAY, date.dayOfMonth)
          }
        }
      }
    }
  }
}
