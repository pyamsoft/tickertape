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

package com.pyamsoft.tickertape.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
@JvmOverloads
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    imageLoader: ImageLoader,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onChartClicked: (Ticker) -> Unit,
    onSettingsClicked: () -> Unit,
) {
  val isLoading = state.isLoading

  Scaffold(
      modifier = modifier,
  ) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isLoading),
        onRefresh = onRefresh,
    ) {
      HomeContent(
          modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState()),
          state = state,
          appName = appName,
          imageLoader = imageLoader,
          navBarBottomHeight = navBarBottomHeight,
          onChartClicked = onChartClicked,
          onSettingsClicked = onSettingsClicked,
      )
    }
  }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    navBarBottomHeight: Int,
    appName: String,
    imageLoader: ImageLoader,
    onChartClicked: (Ticker) -> Unit,
    onSettingsClicked: () -> Unit,
) {
  val density = LocalDensity.current
  val bottomPaddingDp =
      remember(density, navBarBottomHeight) { density.run { navBarBottomHeight.toDp() } }

  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
  ) {
    HomeHeader(
        modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        appName = appName,
        onSettingsClicked = onSettingsClicked,
    )
    HomePortfolio(
        state = state,
    )
    HomeIndexes(
        state = state,
        imageLoader = imageLoader,
        onChartClicked = onChartClicked,
    )
    HomeWatchlist(
        state = state,
        onClicked = onChartClicked,
    )
    HomeTrending(
        state = state,
        imageLoader = imageLoader,
        onChartClicked = onChartClicked,
    )
    HomeGainers(
        state = state,
        imageLoader = imageLoader,
        onChartClicked = onChartClicked,
    )
    HomeLosers(
        state = state,
        imageLoader = imageLoader,
        onChartClicked = onChartClicked,
    )
    HomeMostShorted(
        state = state,
        imageLoader = imageLoader,
        onChartClicked = onChartClicked,
    )

    Spacer(
        modifier =
            Modifier.navigationBarsHeight(
                additional = bottomPaddingDp + MaterialTheme.keylines.content,
            ),
    )
  }
}

@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    appName: String,
    onSettingsClicked: () -> Unit,
) {
  Row(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
        modifier = Modifier.weight(1F),
        text = appName,
        style = MaterialTheme.typography.h4,
    )
    IconButton(
        onClick = onSettingsClicked,
    ) {
      Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = "Settings",
      )
    }
  }
}

@Preview
@Composable
private fun PreviewHomeScreen() {
  HomeScreen(
      state = MutableHomeViewState(),
      imageLoader = createNewTestImageLoader(),
      appName = "TEST",
      onChartClicked = {},
      onRefresh = {},
      onSettingsClicked = {},
  )
}
