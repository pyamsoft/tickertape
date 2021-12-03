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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
@JvmOverloads
@OptIn(ExperimentalAnimationApi::class)
fun MainScreen(
    modifier: Modifier = Modifier,
    page: MainPage,
    onBottomBarHeightMeasured: (Int) -> Unit,
    onLoadHome: () -> Unit,
    onLoadWatchList: () -> Unit,
    onLoadPortfolio: () -> Unit,
    onLoadSettings: () -> Unit,
    onFabClicked: () -> Unit,
) {
  // Enforce a height for the scaffold or else it takes over the screen
  // Why do we need a scaffold instead of a box?
  // Because using a FAB with a BottomAppBar doesn't actually work unless its inside a scaffold.
  //
  // yeah.
  val isFabEnabled = remember(page) { page == MainPage.WatchList || page == MainPage.Portfolio }

  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    AnimatedVisibility(
        visible = isFabEnabled,
    ) {
      MainFab(
          onClick = onFabClicked,
      )
    }
    MainBottomNav(
        page = page,
        onLoadHome = onLoadHome,
        onLoadWatchList = onLoadWatchList,
        onLoadPortfolio = onLoadPortfolio,
        onLoadSettings = onLoadSettings,
        onHeightMeasured = onBottomBarHeightMeasured,
    )
  }
}

@Preview
@Composable
private fun PreviewMainScreen() {
  MainScreen(
      page = MainPage.Home,
      onBottomBarHeightMeasured = {},
      onLoadHome = {},
      onLoadWatchList = {},
      onLoadPortfolio = {},
      onLoadSettings = {},
      onFabClicked = {},
  )
}
