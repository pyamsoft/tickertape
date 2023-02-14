package com.pyamsoft.tickertape.notification

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface NotificationViewState : UiViewState {
  val isBigMoverEnabled: StateFlow<Boolean>
}

@Stable
class MutableNotificationViewState @Inject internal constructor() : NotificationViewState {
  override val isBigMoverEnabled =
      MutableStateFlow(BigMoverPreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED)
}
