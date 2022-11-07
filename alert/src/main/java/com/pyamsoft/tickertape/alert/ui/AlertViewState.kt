package com.pyamsoft.tickertape.alert.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.alert.preference.BigMoverPreferences
import com.pyamsoft.tickertape.tape.TapePreferences
import javax.inject.Inject

@Stable
interface AlertViewState : UiViewState {
  val isTapeEnabled: Boolean
  val tapePageSize: Int

  val isBigMoverEnabled: Boolean
}

@Stable
internal class MutableAlertViewState @Inject internal constructor() : AlertViewState {
  override var isTapeEnabled by mutableStateOf(TapePreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED)
  override var tapePageSize by mutableStateOf(TapePreferences.VALUE_DEFAULT_PAGE_SIZE)

  override var isBigMoverEnabled by
      mutableStateOf(BigMoverPreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED)
}
