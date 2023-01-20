package com.pyamsoft.tickertape.notification

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.notification.components.NotificationCard

@Composable
internal fun BigMoverNotifications(
    modifier: Modifier = Modifier,
    state: NotificationViewState,
    onBigMoverNotificationToggled: () -> Unit,
) {
  val isEnabled by state.isBigMoverEnabled.collectAsState()

  NotificationCard(
      modifier = modifier.padding(horizontal = MaterialTheme.keylines.content),
      title = "Big Mover Alerts",
      isChecked = isEnabled,
      onCheckedChanged = { onBigMoverNotificationToggled() },
      contentDescription =
          "Show alert notifications when a big move happens to a ticker on your watchlist",
  )
}
