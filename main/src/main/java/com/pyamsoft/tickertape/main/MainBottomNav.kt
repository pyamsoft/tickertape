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

package com.pyamsoft.tickertape.main

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.ui.icon.BarChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@CheckResult
fun rememberAllTabs(): SnapshotStateList<MainPage> {
  return remember {
    mutableStateListOf(
        MainPage.Home,
        MainPage.Portfolio,
        MainPage.Notifications,
    )
  }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun MainBottomNav(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
    page: MainPage,
) {
  val scope = rememberCoroutineScope()

  val handleNavigationClicked by rememberUpdatedState { p: MainPage ->
    // Click fires the index to update
    // The index updating is caught by the snapshot flow
    // Which then triggers the page update function
    val index = allTabs.indexOfFirst { it == p }
    scope.launch(context = Dispatchers.Main) { pagerState.animateScrollToPage(index) }
  }

  BottomAppBar(
      modifier = modifier,
      contentColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
      backgroundColor = MaterialTheme.colors.background,
      cutoutShape = null,
      elevation = ZeroElevation,
  ) {
    BottomNavigation(
        modifier = Modifier.weight(1F),
        backgroundColor = Color.Transparent,
        contentColor = LocalContentColor.current,
        elevation = ZeroElevation,
    ) {
      Item(
          current = page,
          target = MainPage.Home,
          onLoadPage = { handleNavigationClicked(it) },
      )
      Item(
          current = page,
          target = MainPage.Portfolio,
          onLoadPage = { handleNavigationClicked(it) },
      )
      Item(
          current = page,
          target = MainPage.Notifications,
          onLoadPage = { handleNavigationClicked(it) },
      )
    }

    // FAB padding
    Spacer(
        modifier = Modifier.width((56 + 16).dp),
    )
  }
}

@Composable
private fun RowScope.Item(
    modifier: Modifier = Modifier,
    current: MainPage,
    target: MainPage,
    onLoadPage: (MainPage) -> Unit,
) {
  val currentColor = LocalContentColor.current

  val icon =
      remember(target) {
        when (target) {
          MainPage.Home -> Icons.Filled.Home
          MainPage.Portfolio -> Icons.Filled.BarChart
          MainPage.Notifications -> Icons.Filled.Notifications
        }
      }

  val isSelected = remember(current, target) { current == target }

  BottomNavigationItem(
      modifier = modifier,
      selected = isSelected,
      onClick = { onLoadPage(target) },
      selectedContentColor =
          MaterialTheme.colors.primary.copy(
              alpha = ContentAlpha.high,
          ),
      unselectedContentColor = currentColor,
      alwaysShowLabel = false,
      icon = {
        Icon(
            imageVector = icon,
            contentDescription = target.displayName,
        )
      },
  )
}
