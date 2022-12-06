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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.pydroid.ui.widget.NewVersionWidget
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import kotlinx.coroutines.CoroutineScope

@Composable
@JvmOverloads
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    imageLoader: ImageLoader,
    navBarBottomHeight: Int = 0,
    onSettingsClicked: () -> Unit,
    onChartClicked: (Ticker) -> Unit,
    onRefreshIndexes: CoroutineScope.() -> Unit,
    onRefreshWatchlist: CoroutineScope.() -> Unit,
    onRefreshPortfolio: CoroutineScope.() -> Unit,
    onRefreshGainers: CoroutineScope.() -> Unit,
    onRefreshLosers: CoroutineScope.() -> Unit,
    onRefreshTrending: CoroutineScope.() -> Unit,
    onRefreshMostActive: CoroutineScope.() -> Unit,
    onRefreshUndervaluedGrowth: CoroutineScope.() -> Unit,
    onRefreshGrowthTech: CoroutineScope.() -> Unit,
    onRefreshMostShorted: CoroutineScope.() -> Unit,
) {
  val density = LocalDensity.current
  val bottomPaddingDp =
      remember(
          density,
          navBarBottomHeight,
      ) {
        density.run { navBarBottomHeight.toDp() }
      }

  Scaffold(
      modifier = modifier,
  ) { pv ->
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {
      item {
        Spacer(
            modifier = Modifier.fillMaxWidth().padding(pv).statusBarsPadding(),
        )
      }
      item {
        NewVersionWidget(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content),
        )
      }

      item {
        HomeHeader(
            modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
            appName = appName,
            onSettingsClicked = onSettingsClicked,
        )
      }

      item {
        HomePortfolio(
            state = state,
            onRefresh = onRefreshPortfolio,
        )
      }

      item {
        HomeIndexes(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshIndexes,
        )
      }

      item {
        HomeWatchlist(
            state = state,
            onClicked = onChartClicked,
            onRefresh = onRefreshWatchlist,
        )
      }

      item {
        HomeTrending(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshTrending,
        )
      }

      item {
        HomeGainers(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshGainers,
        )
      }

      item {
        HomeLosers(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshLosers,
        )
      }

      item {
        HomeMostActive(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshMostActive,
        )
      }

      item {
        HomeUndervaluedGrowth(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshUndervaluedGrowth,
        )
      }

      item {
        HomeGrowthTech(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshGrowthTech,
        )
      }

      item {
        HomeMostShorted(
            state = state,
            imageLoader = imageLoader,
            onChartClicked = onChartClicked,
            onRefresh = onRefreshMostShorted,
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
private fun HomeHeader(
    modifier: Modifier = Modifier,
    appName: String,
    onSettingsClicked: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          modifier = Modifier.weight(1F),
          text = appName,
          style = MaterialTheme.typography.h5,
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
}

@Preview
@Composable
private fun PreviewHomeScreen() {
  HomeScreen(
      state = MutableHomeViewState(),
      imageLoader = createNewTestImageLoader(),
      appName = "TEST",
      onChartClicked = {},
      onSettingsClicked = {},
      onRefreshGainers = {},
      onRefreshGrowthTech = {},
      onRefreshIndexes = {},
      onRefreshLosers = {},
      onRefreshMostActive = {},
      onRefreshMostShorted = {},
      onRefreshPortfolio = {},
      onRefreshTrending = {},
      onRefreshUndervaluedGrowth = {},
      onRefreshWatchlist = {},
  )
}
