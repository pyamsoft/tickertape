/*
 * Copyright 2021 Peter Kenji Yamanaka
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.notification.components.ColoredCard
import com.pyamsoft.tickertape.notification.components.NotificationCard
import com.pyamsoft.tickertape.notification.components.NotificationCardSize
import com.pyamsoft.tickertape.notification.components.NotificationOption

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    state: NotificationViewState,
    navBarBottomHeight: Int,
    onTapeNotificationToggled: () -> Unit,
    onPageSizeChanged: (Int) -> Unit,
) {
  val density = LocalDensity.current

  val bottomPaddingDp =
      remember(
          density,
          navBarBottomHeight,
      ) {
        density.run { navBarBottomHeight.toDp() }
      }

  val scaffoldState = rememberScaffoldState()

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
  ) { pv ->
    LazyColumn {
      item {
        Spacer(
            modifier = Modifier.padding(pv).statusBarsPadding(),
        )
      }

      item {
        TapeNotifications(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            onTapeNotificationToggled = onTapeNotificationToggled,
            onPageSizeChanged = onPageSizeChanged,
        )
      }

      item {
        Spacer(
            modifier = Modifier.padding(pv).navigationBarsPadding().height(bottomPaddingDp),
        )
      }
    }
  }
}

@Composable
private fun TapeNotifications(
    modifier: Modifier = Modifier,
    state: NotificationViewState,
    onTapeNotificationToggled: () -> Unit,
    onPageSizeChanged: (Int) -> Unit,
) {
  val isEnabled = state.isTapeEnabled

  NotificationCard(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      size = NotificationCardSize.LARGE,
      title = "Tape Notifications",
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

@Preview
@Composable
private fun PreviewNotificationScreen() {
  NotificationScreen(
      state = MutableNotificationViewState(),
      navBarBottomHeight = 0,
      onTapeNotificationToggled = {},
      onPageSizeChanged = {},
  )
}
