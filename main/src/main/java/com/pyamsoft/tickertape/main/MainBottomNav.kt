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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.ui.FabDefaults
import com.pyamsoft.tickertape.ui.icon.BarChart
import com.pyamsoft.tickertape.ui.icon.PieChart

@Composable
internal fun MainBottomNav(
    modifier: Modifier = Modifier,
    page: TopLevelMainPage,
    onLoadHome: () -> Unit,
    onLoadWatchlist: () -> Unit,
    onLoadPortfolio: () -> Unit,
    onHeightMeasured: (Int) -> Unit,
    onActionSelected: (TopLevelMainPage) -> Unit,
) {
  // Additional padding when taking into account the "height" of the bar
  //
  // We add this here instead of applying at the usage site because a usage site should not care
  // about padding from an implementation of the BottomNav, it should be given a "safe area" and
  // simply draw into it
  val density = LocalDensity.current
  val keylines = MaterialTheme.keylines
  val additionalTopContentPadding =
      remember(
          density,
          keylines,
      ) {
        density.run { keylines.content.roundToPx() }
      }

  val (fabOffset, setFabOffset) = remember { mutableStateOf(0) }
  val fabOffsetDp =
      remember(
          density,
          fabOffset,
      ) {
        density.run { fabOffset.toDp() }
      }

  // We set the height here to the size of the bar (which is also FAB_SIZE_DP) + the size of the
  // button when it peeks outside of the bar (FAB_SIZE_DP / 2)
  //
  // Failing to set the height here will make the bar jump up and down when the page scrolls from
  // no-show-fab to show-fab
  Box(
      modifier = modifier.height(FabDefaults.FAB_SIZE_DP + FabDefaults.FAB_SIZE_DP / 2),
      contentAlignment = Alignment.BottomEnd,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      // Can't use BottomAppBar since we can't modify its Shape
      Surface(
          modifier =
              Modifier.fillMaxWidth().onSizeChanged {
                onHeightMeasured(it.height + additionalTopContentPadding)
                setFabOffset(it.height / 2)
              },
          contentColor = MaterialTheme.colors.onSurface,
          color = MaterialTheme.colors.surface,
          shape =
              MaterialTheme.shapes.medium.copy(
                  bottomEnd = ZeroCornerSize,
                  bottomStart = ZeroCornerSize,
              ),
          elevation = AppBarDefaults.BottomAppBarElevation,
      ) {
        Row(
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
                onLoadPage = onLoadHome,
            )
            Item(
                current = page,
                target = TopLevelMainPage.Watchlist,
                onLoadPage = onLoadWatchlist,
            )
            Item(
                current = page,
                target = TopLevelMainPage.Portfolio,
                onLoadPage = onLoadPortfolio,
            )
          }

          Spacer(
              modifier =
                  Modifier.padding(horizontal = MaterialTheme.keylines.content)
                      .padding(end = MaterialTheme.keylines.content)
                      .width(FabDefaults.FAB_SIZE_DP),
          )
        }
      }
      Spacer(
          modifier = Modifier.navigationBarsPadding(),
      )
    }

    // Float on top of the bar
    ActionButton(
        modifier =
            Modifier.padding(horizontal = MaterialTheme.keylines.content)
                .padding(end = MaterialTheme.keylines.content)
                .padding(bottom = fabOffsetDp)
                .width(FabDefaults.FAB_SIZE_DP),
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
  Box(
      modifier = modifier,
  ) {
    AnimatedVisibility(
        visible = page.showFab,
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
    onLoadPage: () -> Unit,
) {

  val icon =
      remember(target) {
        when (target) {
          TopLevelMainPage.Home -> Icons.Filled.Home
          TopLevelMainPage.Watchlist -> Icons.Filled.BarChart
          TopLevelMainPage.Portfolio -> Icons.Filled.PieChart
        }
      }

  val isSelected = remember(current, target) { current == target }

  val colors = MaterialTheme.colors
  val mediumAlpha = ContentAlpha.medium
  val highAlpha = ContentAlpha.high
  val color =
      remember(
          isSelected,
          colors,
          mediumAlpha,
          highAlpha,
      ) {
        if (isSelected) {
          colors.primary.copy(
              alpha = highAlpha,
          )
        } else {
          colors.onSurface.copy(
              alpha = mediumAlpha,
          )
        }
      }

  BottomNavigationItem(
      modifier = modifier,
      selected = isSelected,
      onClick = { onLoadPage() },
      icon = {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
              imageVector = icon,
              contentDescription = target.displayName,
              tint = color,
          )
          Text(
              text = target.displayName,
              style =
                  MaterialTheme.typography.body2.copy(
                      color = color,
                  ),
          )
        }
      },
  )
}

@Preview
@Composable
private fun PreviewMainBottomNav() {
  MainBottomNav(
      page = TopLevelMainPage.Home,
      onHeightMeasured = {},
      onLoadHome = {},
      onLoadWatchlist = {},
      onLoadPortfolio = {},
      onActionSelected = {},
  )
}
