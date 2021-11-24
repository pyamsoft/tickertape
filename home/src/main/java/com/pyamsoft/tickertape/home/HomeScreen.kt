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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.tickertape.quote.Ticker

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    onRefresh: () -> Unit,
    onChartClicked: (Ticker) -> Unit,
) {
  val isLoading = state.isLoading

  SwipeRefresh(
      modifier = modifier,
      state = rememberSwipeRefreshState(isRefreshing = isLoading),
      onRefresh = onRefresh,
  ) {
    HomeContent(
        modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState()),
        state = state,
        onChartClicked = onChartClicked,
    )
  }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    onChartClicked: (Ticker) -> Unit,
) {
  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    HomeIndexes(
        state = state,
        onChartClicked = onChartClicked,
    )
    HomeTrending(
        state = state,
        onChartClicked = onChartClicked,
    )
    HomeGainers(
        state = state,
        onChartClicked = onChartClicked,
    )
    HomeLosers(
        state = state,
        onChartClicked = onChartClicked,
    )
    HomeMostShorted(
        state = state,
        onChartClicked = onChartClicked,
    )
  }
}

@Preview
@Composable
private fun PreviewHomeScreen() {
  Surface {
    HomeScreen(
        state = MutableHomeViewState(),
        onChartClicked = {},
        onRefresh = {},
    )
  }
}
