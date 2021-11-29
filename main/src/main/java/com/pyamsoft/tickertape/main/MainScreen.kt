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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDefaults
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@JvmOverloads
@OptIn(ExperimentalAnimationApi::class)
fun MainScreen(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState,
    page: MainPage,
    onBottomBarHeightMeasured: (Int) -> Unit,
    onLoadHome: () -> Unit,
    onLoadWatchList: () -> Unit,
    onLoadPortfolio: () -> Unit,
    onLoadSettings: () -> Unit,
    onFabClicked: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
  // Enforce a height for the scaffold or else it takes over the screen
  // Why do we need a scaffold instead of a box?
  // Because using a FAB with a BottomAppBar doesn't actually work unless its inside a scaffold.
  //
  // yeah.
  val isFabEnabled = remember(page) { page == MainPage.WatchList || page == MainPage.Portfolio }

  // The FAB shape
  val fabShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50))
  Scaffold(
      // Need padding on the scaffold or else the bar wont float like we want it to
      // but we need it here instead of on the bottom app bar itself or else the cutout and the
      // FAB will be misplaced. This is an awful API.
      modifier = modifier.padding(horizontal = 16.dp),
      backgroundColor = Color.Transparent,
      scaffoldState = scaffoldState,
      floatingActionButton = {
        AnimatedVisibility(
            visible = isFabEnabled,
        ) {
          MainFab(
              fabShape = fabShape,
              onClick = onFabClicked,
          )
        }
      },
      snackbarHost = { hostState ->
        SnackbarHost(
            hostState = hostState,
            snackbar = { data ->
              CustomSnackbar(
                  snackbarData = data,
              )
            },
        )
      },
      floatingActionButtonPosition = FabPosition.Center,
      isFloatingActionButtonDocked = true,
      bottomBar = {
        MainBottomNav(
            cutoutShape = fabShape,
            page = page,
            onLoadHome = onLoadHome,
            onLoadWatchList = onLoadWatchList,
            onLoadPortfolio = onLoadPortfolio,
            onLoadSettings = onLoadSettings,
            onHeightMeasured = onBottomBarHeightMeasured,
        )
      },
      content = content,
  )
}

// We copy this literally so that we can remove horizontal padding.
// Ye.
@Composable
private fun CustomSnackbar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData,
    actionOnNewLine: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    backgroundColor: Color = SnackbarDefaults.backgroundColor,
    contentColor: Color = MaterialTheme.colors.surface,
    actionColor: Color = SnackbarDefaults.primaryActionColor,
    elevation: Dp = 6.dp
) {
  val actionLabel = snackbarData.actionLabel
  val actionComposable: (@Composable () -> Unit)? =
      if (actionLabel != null) {
        @Composable
        {
          TextButton(
              colors = ButtonDefaults.textButtonColors(contentColor = actionColor),
              onClick = { snackbarData.performAction() },
              content = { Text(actionLabel) })
        }
      } else {
        null
      }

  // No horizontal padding, the Scaffold is already padded for us
  Snackbar(
      modifier = modifier.padding(vertical = 12.dp),
      content = { Text(snackbarData.message) },
      action = actionComposable,
      actionOnNewLine = actionOnNewLine,
      shape = shape,
      backgroundColor = backgroundColor,
      contentColor = contentColor,
      elevation = elevation)
}

@Preview
@Composable
private fun PreviewMainScreen() {
  MainScreen(
      scaffoldState = rememberScaffoldState(),
      page = MainPage.Home,
      onBottomBarHeightMeasured = {},
      onLoadHome = {},
      onLoadWatchList = {},
      onLoadPortfolio = {},
      onLoadSettings = {},
      onFabClicked = {},
      content = {},
  )
}
