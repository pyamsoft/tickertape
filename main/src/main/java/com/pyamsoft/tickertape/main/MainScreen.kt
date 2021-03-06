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

package com.pyamsoft.tickertape.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines

@Composable
@JvmOverloads
fun MainScreen(
    modifier: Modifier = Modifier,
    page: TopLevelMainPage,
    onLoadHome: () -> Unit,
    onLoadWatchlist: () -> Unit,
    onLoadPortfolio: () -> Unit,
    onBottomBarHeightMeasured: (Int) -> Unit,
) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    MainBottomNav(
        modifier =
            Modifier.padding(horizontal = MaterialTheme.keylines.content)
                .padding(bottom = MaterialTheme.keylines.content),
        page = page,
        onLoadHome = onLoadHome,
        onLoadWatchlist = onLoadWatchlist,
        onLoadPortfolio = onLoadPortfolio,
        onHeightMeasured = onBottomBarHeightMeasured,
    )
  }
}

@Preview
@Composable
private fun PreviewMainScreen() {
  MainScreen(
      page = TopLevelMainPage.Home,
      onBottomBarHeightMeasured = {},
      onLoadHome = {},
      onLoadWatchlist = {},
      onLoadPortfolio = {},
  )
}
