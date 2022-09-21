package com.pyamsoft.tickertape.portfolio.dig.position.date

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.base.DateSelectedEvent
import com.pyamsoft.tickertape.ui.BaseDateDialog
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject

internal class PositionDateDialog : BaseDateDialog() {

  @JvmField
  @Inject
  internal var datePickerEventBus: EventBus<DateSelectedEvent<DbPosition.Id>>? = null

  @CheckResult
  private fun getPositionId(): DbPosition.Id {
    val key = KEY_POSITION
    return requireArguments()
        .getString(key, "")
        .also { require(it.isNotBlank()) { "Must create with key: $key" } }
        .let { DbPosition.Id(it) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.obtainFromApplication<TickerComponent>(requireContext())
        .plusPositionDateComponent()
        .create(getPositionId())
        .inject(this)
  }

  override suspend fun onDateSelected(year: Int, month: Month, dayOfMonth: Int) {
    datePickerEventBus
        .requireNotNull()
        .send(
            DateSelectedEvent(
                id = getPositionId(),
                year = year,
                month = month,
                dayOfMonth = dayOfMonth,
            ),
        )
  }

  override fun onDestroy() {
    super.onDestroy()
    datePickerEventBus = null
  }

  companion object {

    private val TAG = PositionDateDialog::class.java.name
    private const val KEY_POSITION = "item"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        positionId: DbPosition.Id,
        year: Int,
        month: Month,
        day: Int,
    ): DialogFragment {
      return PositionDateDialog().apply {
        arguments =
            prepareBundle(year, month, day).apply { putString(KEY_POSITION, positionId.raw) }
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
