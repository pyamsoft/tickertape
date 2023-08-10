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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.pyamsoft.tickertape.home.HomeEntry
import com.pyamsoft.tickertape.notification.NotificationEntry
import com.pyamsoft.tickertape.portfolio.PortfolioEntry
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.quote.Ticker

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun MainContent(
    modifier: Modifier = Modifier,
    appName: String,
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
    onHomeDig: (Ticker) -> Unit,
    onPortfolioDig: (PortfolioStock) -> Unit,
) {
  HorizontalPager(
      modifier = modifier,
      state = pagerState,
  ) { page ->
    val screen =
        remember(
            allTabs,
            page,
        ) {
          allTabs[page]
        }

    when (screen) {
      is MainPage.Home -> {
        HomeEntry(
            modifier = Modifier.fillMaxSize(),
            appName = appName,
            onDig = onHomeDig,
        )
      }
      is MainPage.Portfolio -> {
        PortfolioEntry(
            modifier = Modifier.fillMaxSize(),
            onDig = onPortfolioDig,
        )
      }
      is MainPage.Notifications -> {
        NotificationEntry(
            modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}
