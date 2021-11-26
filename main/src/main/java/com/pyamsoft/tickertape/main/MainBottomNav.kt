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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight

@Composable
internal fun MainBottomNav(
    modifier: Modifier = Modifier,
    page: MainPage,
    cutoutShape: Shape,
    onHeightMeasured: (Int) -> Unit,
    onLoadHome: () -> Unit,
    onLoadWatchList: () -> Unit,
    onLoadPortfolio: () -> Unit,
    onLoadSettings: () -> Unit,
) {
  // Can't use BottomAppBar since we can't modify its Shape
  val padding = 16.dp
  val density = LocalDensity.current
  val paddingInPx = remember(density) { density.run { padding.roundToPx() } }

  Column {
    BottomAppBar(
        modifier =
            modifier.padding(bottom = padding).onSizeChanged {
              onHeightMeasured(it.height + paddingInPx)
            },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        cutoutShape = cutoutShape,
    ) {
      BottomNavigation(
          backgroundColor = Color.Transparent,
          contentColor = LocalContentColor.current,
          elevation = 0.dp,
      ) {
        Item(
            current = page,
            target = MainPage.Home,
            onClick = onLoadHome,
        )
        Item(
            current = page,
            target = MainPage.WatchList,
            onClick = onLoadWatchList,
        )
        Placeholder()
        Item(
            current = page,
            target = MainPage.Portfolio,
            onClick = onLoadPortfolio,
        )
        Item(
            current = page,
            target = MainPage.Settings,
            onClick = onLoadSettings,
        )
      }
    }
    Spacer(
        modifier = Modifier.navigationBarsHeight(),
    )
  }
}

@Composable
private fun RowScope.Placeholder(
    modifier: Modifier = Modifier,
) {
  BottomNavigationItem(
      modifier = modifier,
      enabled = false,
      selected = false,
      onClick = {},
      icon = {},
  )
}

@Composable
private fun RowScope.Item(
    modifier: Modifier = Modifier,
    current: MainPage,
    target: MainPage,
    onClick: () -> Unit,
) {
  BottomNavigationItem(
      modifier = modifier,
      selected = current == target,
      onClick = onClick,
      icon = {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
              imageVector =
                  when (target) {
                    is MainPage.Home -> Icons.Filled.Home
                    is MainPage.WatchList -> Icons.Filled.BarChart
                    is MainPage.Portfolio -> Icons.Filled.PieChart
                    is MainPage.Settings -> Icons.Filled.Settings
                  },
              contentDescription = target.name,
          )
          Text(
              text = target.name,
              style = MaterialTheme.typography.body2,
          )
        }
      },
  )
}

@Preview
@Composable
private fun PreviewMainBottomNav() {
  MainBottomNav(
      cutoutShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
      page = MainPage.Home,
      onHeightMeasured = {},
      onLoadHome = {},
      onLoadWatchList = {},
      onLoadPortfolio = {},
      onLoadSettings = {},
  )
}
