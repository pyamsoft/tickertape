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
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.insets.navigationBarsHeight
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.ui.icon.BarChart
import com.pyamsoft.tickertape.ui.icon.PieChart

@Composable
internal fun MainBottomNav(
    modifier: Modifier = Modifier,
    page: TopLevelScreen,
    onLoadPage: (TopLevelScreen) -> Unit,
    onHeightMeasured: (Int) -> Unit,
) {
  // Can't use BottomAppBar since we can't modify its Shape
  Column {
    Surface(
        modifier = modifier.onSizeChanged { onHeightMeasured(it.height) },
        contentColor = Color.White,
        color = MaterialTheme.colors.primary,
        shape = MaterialTheme.shapes.medium,
        elevation = AppBarDefaults.BottomAppBarElevation,
    ) {
      BottomNavigation(
          backgroundColor = Color.Transparent,
          contentColor = LocalContentColor.current,
          elevation = ZeroElevation,
      ) {
        Item(
            current = page,
            target = MainPage.Home,
            onLoadPage = onLoadPage,
        )
        Item(
            current = page,
            target = MainPage.WatchList,
            onLoadPage = onLoadPage,
        )
        Item(
            current = page,
            target = MainPage.Portfolio,
            onLoadPage = onLoadPage,
        )
      }
    }
    Spacer(
        modifier = Modifier.navigationBarsHeight(),
    )
  }
}

@Composable
private fun RowScope.Item(
    modifier: Modifier = Modifier,
    current: TopLevelScreen,
    target: TopLevelScreen,
    onLoadPage: (TopLevelScreen) -> Unit,
) {

  val icon =
      remember(target) {
        when (target) {
          is MainPage.Home -> Icons.Filled.Home
          is MainPage.WatchList -> Icons.Filled.BarChart
          is MainPage.Portfolio -> Icons.Filled.PieChart
          else -> throw IllegalArgumentException("Unhandled TopLevel page: $target")
        }
      }

  BottomNavigationItem(
      modifier = modifier,
      selected = current == target,
      onClick = { onLoadPage(target) },
      icon = {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
              imageVector = icon,
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
      page = MainPage.Home,
      onHeightMeasured = {},
      onLoadPage = {},
  )
}
