package com.pyamsoft.tickertape.portfolio.dig.position.date

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.pydroid.util.valueFromCurrentTheme
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.position.add.DatePickerEvent
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class PositionAddDateDialog : AppCompatDialogFragment() {

  @JvmField @Inject internal var datePickerEventBus: EventBus<DatePickerEvent>? = null

  @CheckResult
  private fun getPositionId(): DbPosition.Id {
    val key = KEY_POSITION
    return requireArguments()
        .getString(key, "")
        .also { require(it.isNotBlank()) { "Must create with key: $key" } }
        .let { DbPosition.Id(it) }
  }

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
    val backgroundId = act.valueFromCurrentTheme(android.R.attr.windowBackground)
    return AppCompatResources.getDrawable(act, backgroundId).requireNotNull()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.obtainFromApplication<TickerComponent>(requireContext())
        .plusPositionAddDateComponent()
        .create(getPositionId())
        .inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val initialYear = getYear()
    val initialMonth = getMonth()
    val initialDay = getDay()

    val listener =
        DatePickerDialog.OnDateSetListener { _, year, monthValue, dayOfMonth ->
          // Calendar month is 0-11, but LDT month is 1-12
          val month = Month.of(monthValue + 1)

          // Make sure we use the ActivityScope for this operation
          requireActivity().lifecycleScope.launch(context = Dispatchers.Default) {
            datePickerEventBus
                .requireNotNull()
                .send(
                    DatePickerEvent(
                        positionId = getPositionId(),
                        year = year,
                        month = month,
                        dayOfMonth = dayOfMonth,
                    ),
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

  override fun onDestroy() {
    super.onDestroy()
    datePickerEventBus = null
  }

  companion object {

    private const val TAG = "ExpandDateSelectDialog"
    private const val KEY_POSITION = "item"
    private const val KEY_YEAR = "year"
    private const val KEY_MONTH = "month"
    private const val KEY_DAY = "day"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        positionId: DbPosition.Id,
        year: Int,
        month: Month,
        day: Int,
    ): DialogFragment {
      return PositionAddDateDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_POSITION, positionId.id)
              putInt(KEY_YEAR, year)
              putString(KEY_MONTH, month.name)
              putInt(KEY_DAY, day)
            }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        positionId: DbPosition.Id,
        purchaseDate: LocalDate?,
    ) {
      val today = purchaseDate ?: LocalDate.now()
      newInstance(
              positionId,
              today.year,
              today.month,
              today.dayOfMonth,
          )
          .show(activity, TAG)
    }
  }
}
