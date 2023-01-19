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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.ui.FabDefaults
import com.pyamsoft.tickertape.ui.icon.BarChart
import com.pyamsoft.tickertape.ui.icon.PieChart

@Composable
internal fun MainBottomNav(
    modifier: Modifier = Modifier,
    page: TopLevelMainPage,
    onLoadPage: (TopLevelMainPage) -> Unit,
    onActionSelected: (TopLevelMainPage) -> Unit,
) {
  // Space on the bottom bar for the FAB
  val fabSpacerModifier =
      Modifier.padding(horizontal = MaterialTheme.keylines.content).width(FabDefaults.FAB_SIZE_DP)

  // This Box is aligned like this as a Column
  //
  // SPACE - Half a FAB
  // divider
  // Surface
  //   -- containing BottomNav and navbar padding
  Box(
      modifier = modifier,
      contentAlignment = Alignment.TopEnd,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      val color = MaterialTheme.colors.onBackground

      // Space for FAB
      // without this, top of FAB will be clipped
      Spacer(
          modifier = Modifier.height(FabDefaults.FAB_SIZE_DP / 2),
      )

      // Divider, visual for page content
      Divider(
          color = color.copy(alpha = ContentAlpha.disabled),
          thickness = 2.dp,
      )

      // Can't use BottomAppBar since we can't modify its Shape
      // Even though we use Rectangle (so we don't modify shape)
      // I like Surface since it won't change API behavior like bottom app bar may
      Surface(
          modifier = Modifier.fillMaxWidth(),
          contentColor = color.copy(alpha = ContentAlpha.medium),
          color = MaterialTheme.colors.background,
          shape = RectangleShape,
          elevation = ZeroElevation,
      ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            BottomNavigation(
                modifier = Modifier.weight(1F),
                backgroundColor = Color.Transparent,
                contentColor = LocalContentColor.current,
                elevation = ZeroElevation,
            ) {
              Item(
                  current = page,
                  target = TopLevelMainPage.Home,
                  onLoadPage = onLoadPage,
              )
              Item(
                  current = page,
                  target = TopLevelMainPage.Watchlist,
                  onLoadPage = onLoadPage,
              )
              Item(
                  current = page,
                  target = TopLevelMainPage.Portfolio,
                  onLoadPage = onLoadPage,
              )
              Item(
                  current = page,
                  target = TopLevelMainPage.Notifications,
                  onLoadPage = onLoadPage,
              )
            }

            // Leave this space forced empty for the FAB
            // Since BottomNav stretches to fill space, we have to force it to be occupied
            Spacer(
                modifier = fabSpacerModifier,
            )
          }

          // Navbar padding inside of the colored surface
          Spacer(
              modifier = Modifier.navigationBarsPadding(),
          )
        }
      }
    }

    // Float on top of the bar
    ActionButton(
        modifier = fabSpacerModifier,
        page = page,
        onActionSelected = onActionSelected,
    )
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun ActionButton(
    modifier: Modifier = Modifier,
    page: TopLevelMainPage,
    onActionSelected: (TopLevelMainPage) -> Unit,
) {

  val isFabVisible =
      remember(page) { page == TopLevelMainPage.Portfolio || page == TopLevelMainPage.Watchlist }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.TopCenter,
  ) {
    AnimatedVisibility(
        visible = isFabVisible,
        // Normal FAB animation
        // https://stackoverflow.com/questions/71141501/cant-animate-fab-visible-in-m3-scaffold
        enter = scaleIn(),
        exit = scaleOut(),
    ) {
      FloatingActionButton(
          backgroundColor = MaterialTheme.colors.primary,
          contentColor = Color.White,
          onClick = { onActionSelected(page) },
      ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
        )
      }
    }
  }
}

@Composable
private fun RowScope.Item(
    modifier: Modifier = Modifier,
    current: TopLevelMainPage,
    target: TopLevelMainPage,
    onLoadPage: (TopLevelMainPage) -> Unit,
) {
  val currentColor = LocalContentColor.current

  val icon =
      remember(target) {
        when (target) {
          TopLevelMainPage.Home -> Icons.Filled.Home
          TopLevelMainPage.Watchlist -> Icons.Filled.BarChart
          TopLevelMainPage.Portfolio -> Icons.Filled.PieChart
          TopLevelMainPage.Notifications -> Icons.Filled.Notifications
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

@Preview
@Composable
private fun PreviewMainBottomNav() {
  MainBottomNav(
      page = TopLevelMainPage.Home,
      onLoadPage = {},
      onActionSelected = {},
  )
}
