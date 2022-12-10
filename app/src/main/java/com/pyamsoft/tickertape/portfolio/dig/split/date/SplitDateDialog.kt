package com.pyamsoft.tickertape.portfolio.dig.split.date

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.base.DateSelectedEvent
import com.pyamsoft.tickertape.ui.BaseDateDialog
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject

internal class SplitDateDialog : BaseDateDialog() {

  @JvmField @Inject internal var datePickerEventBus: EventBus<DateSelectedEvent<DbSplit.Id>>? = null

  @CheckResult
  private fun getSplitId(): DbSplit.Id {
    val key = KEY_SPLIT
    return requireArguments()
        .getString(key, "")
        .also { require(it.isNotBlank()) { "Must create with key: $key" } }
        .let { DbSplit.Id(it) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ObjectGraph.ApplicationScope.retrieve(requireActivity())
        .plusSplitDateComponent()
        .create(getSplitId())
        .inject(this)
  }

  override suspend fun onDateSelected(year: Int, month: Month, dayOfMonth: Int) {
    datePickerEventBus
        .requireNotNull()
        .send(
            DateSelectedEvent(
                id = getSplitId(),
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

    private val TAG = SplitDateDialog::class.java.name
    private const val KEY_SPLIT = "split"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        splitId: DbSplit.Id,
        year: Int,
        month: Month,
        day: Int,
    ): DialogFragment {
      return SplitDateDialog().apply {
        arguments = prepareBundle(year, month, day).apply { putString(KEY_SPLIT, splitId.raw) }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        splitId: DbSplit.Id,
        splitDate: LocalDate?,
    ) {
      val today = splitDate ?: LocalDate.now()
      newInstance(
              splitId,
              today.year,
              today.month,
              today.dayOfMonth,
          )
          .show(activity, TAG)
    }
  }
}
