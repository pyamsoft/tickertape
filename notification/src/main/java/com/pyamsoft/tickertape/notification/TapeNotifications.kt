package com.pyamsoft.tickertape.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.notification.components.ColoredCard
import com.pyamsoft.tickertape.notification.components.NotificationCard
import com.pyamsoft.tickertape.notification.components.NotificationOption

@Composable
internal fun TapeNotifications(
    modifier: Modifier = Modifier,
    state: NotificationViewState,
    onTapeNotificationToggled: () -> Unit,
    onPageSizeChanged: (Int) -> Unit,
) {
  val isEnabled = state.isTapeEnabled

  NotificationCard(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      title = "Tape Notification",
      isChecked = isEnabled,
      onCheckedChanged = { onTapeNotificationToggled() },
      contentDescription = "Show long-running notification with your current watchlist",
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      TapePageSize(
          modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
          isTapeEnabled = isEnabled,
          pageSize = state.tapePageSize,
          onPageSizeChanged = onPageSizeChanged,
      )
    }
  }
}

@Composable
private fun TapePageSize(
    modifier: Modifier = Modifier,
    isTapeEnabled: Boolean,
    pageSize: Int,
    onPageSizeChanged: (Int) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    NotificationOption(
        modifier = Modifier.fillMaxWidth(),
        title = "Page Size",
        isEnabled = isTapeEnabled,
        description = "Number of tickers to show per page",
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.baseline),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      TapePageOption(
          isEnabled = isTapeEnabled,
          pageSize = pageSize,
          onPageSizeChanged = onPageSizeChanged,
          size = 3,
      )

      TapePageOption(
          isEnabled = isTapeEnabled,
          pageSize = pageSize,
          onPageSizeChanged = onPageSizeChanged,
          size = 4,
      )

      TapePageOption(
          isEnabled = isTapeEnabled,
          pageSize = pageSize,
          onPageSizeChanged = onPageSizeChanged,
          size = 5,
      )
    }
  }
}

@Composable
private fun TapePageOption(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    pageSize: Int,
    size: Int,
    onPageSizeChanged: (Int) -> Unit,
) {
  val isChecked =
      remember(
          pageSize,
          size,
      ) {
        pageSize == size
      }

  val color =
      if (isEnabled) if (isChecked) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
      else MaterialTheme.colors.onSurface

  ColoredCard(
      modifier = modifier,
      isEnabled = isEnabled,
      isChecked = isChecked,
  ) {
    Column(
        modifier =
            Modifier.clickable(enabled = isEnabled) { onPageSizeChanged(size) }
                .padding(MaterialTheme.keylines.content),
    ) {
      Text(
          text = "$size",
          style =
              MaterialTheme.typography.h6.copy(
                  fontWeight = if (isChecked) FontWeight.W700 else FontWeight.W400,
                  color =
                      color.copy(
                          alpha =
                              if (isEnabled)
                                  if (isChecked) ContentAlpha.high else ContentAlpha.medium
                              else ContentAlpha.disabled,
                      ),
              ),
      )
    }
  }
}
