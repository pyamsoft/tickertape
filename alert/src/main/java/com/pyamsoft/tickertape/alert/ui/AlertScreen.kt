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

package com.pyamsoft.tickertape.alert.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.alert.ui.components.AlertCard

@Composable
fun AlertScreen(
    modifier: Modifier = Modifier,
    state: AlertViewState,
    navBarBottomHeight: Int,
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
    state: AlertViewState,
) {
  AlertCard(
      modifier = modifier,
      title = "Tape Notifications",
      isChecked = state.isTapeEnabled,
      onCheckedChanged = {},
      contentDescription = "Show long-running notification with your current watchlist",
  ) {
    Column(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
    ) {
      Text(
          text = "Page Size",
          style = MaterialTheme.typography.body2,
      )

      Text(
          text = "Number of tickers to show per page",
          style = MaterialTheme.typography.caption,
      )
    }
  }
}

@Preview
@Composable
private fun PreviewAlertScreen() {
  AlertScreen(
      state = MutableAlertViewState(),
      navBarBottomHeight = 0,
  )
}
