package com.pyamsoft.tickertape.notification

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.tape.TapePreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface NotificationViewState : UiViewState {
  val isTapeEnabled: StateFlow<Boolean>
  val tapePageSize: StateFlow<Int>
  val isBigMoverEnabled: StateFlow<Boolean>
}

@Stable
class MutableNotificationViewState @Inject internal constructor() : NotificationViewState {
  override val isTapeEnabled = MutableStateFlow(TapePreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED)
  override val tapePageSize = MutableStateFlow(TapePreferences.VALUE_DEFAULT_PAGE_SIZE)
  override val isBigMoverEnabled =
      MutableStateFlow(BigMoverPreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED)
}
