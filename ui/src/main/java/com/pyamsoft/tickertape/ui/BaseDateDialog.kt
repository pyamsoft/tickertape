package com.pyamsoft.tickertape.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.attributeFromCurrentTheme
import java.time.Month
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseDateDialog protected constructor() : AppCompatDialogFragment() {

  @CheckResult
  private fun getYear(): Int {
    val key = KEY_YEAR
    return requireArguments().getInt(key, -1).also {
      require(it >= 0) { "Must be called with key $key" }
    }
  }

  @CheckResult
  private fun getMonth(): Month {
    val key = KEY_MONTH
    return requireArguments()
        .getString(key, "")
        .also { require(it.isNotBlank()) { "Must be called with key $key" } }
        .let { Month.valueOf(it) }
  }

  @CheckResult
  private fun getDay(): Int {
    val key = KEY_DAY
    return requireArguments().getInt(key, -1).also {
      require(it >= 0) { "Must be called with key $key" }
    }
  }

  // Because the dialog background is transparent, we need to manually pull out the window
  // background
  // as a drawable and insert it here
  @CheckResult
  private fun createDialogBackground(): Drawable {
    val act = requireActivity()
    val backgroundId = act.attributeFromCurrentTheme(android.R.attr.windowBackground)
    return AppCompatResources.getDrawable(act, backgroundId).requireNotNull()
  }

  final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val initialYear = getYear()
    val initialMonth = getMonth()
    val initialDay = getDay()

    val listener =
        DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
          // Calendar month is 0-11, but LDT month is 1-12
          val month = Month.of(monthValue + 1)

          // Make sure we use the ActivityScope for this operation
          requireActivity().lifecycleScope.launch(context = Dispatchers.Default) {
            onDateSelected(
                year = year,
                month = month,
                dayOfMonth = dayOfMonth,
            )
          }
        }

    return DatePickerDialog(
            requireActivity(),
            listener,
            initialYear,
            // LDT month is 1-12, but calendar is 0-11
            initialMonth.value - 1,
            initialDay,
        )
        .apply { datePicker.background = createDialogBackground() }
  }

  protected abstract suspend fun onDateSelected(
      year: Int,
      month: Month,
      dayOfMonth: Int,
  )

  companion object {

    private const val KEY_YEAR = "year"
    private const val KEY_MONTH = "month"
    private const val KEY_DAY = "day"

    @JvmStatic
    @CheckResult
    protected fun prepareBundle(
        year: Int,
        month: Month,
        day: Int,
    ): Bundle {
      return Bundle().apply {
        putInt(KEY_YEAR, year)
        putString(KEY_MONTH, month.name)
        putInt(KEY_DAY, day)
      }
    }
  }
}
