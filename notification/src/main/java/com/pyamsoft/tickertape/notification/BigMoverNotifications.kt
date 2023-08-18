/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.notification

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
  val isEnabled by state.isBigMoverEnabled.collectAsStateWithLifecycle()

  NotificationCard(
      modifier = modifier.padding(horizontal = MaterialTheme.keylines.content),
      title = "Big Mover Alerts",
      isChecked = isEnabled,
      onCheckedChanged = { onBigMoverNotificationToggled() },
      contentDescription =
          "Show alert notifications when a big move happens to a ticker on your watchlist",
  )
}
