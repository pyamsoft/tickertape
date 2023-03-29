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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigEntry
import com.pyamsoft.tickertape.quote.Ticker

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun MainScreen(
    modifier: Modifier = Modifier,
    appName: String,
    state: MainViewState,
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
    onActionSelected: (MainPage) -> Unit,
    onHomeDig: (Ticker) -> Unit,
    onPortfolioDig: (PortfolioStock) -> Unit,
    onCloseDig: () -> Unit,
) {
  val portfolioDig by state.portfolioDigParams.collectAsState()

  val currentPage = pagerState.currentPage
  val page =
      remember(
          currentPage,
          allTabs,
      ) {
        allTabs[currentPage]
      }

  val showBottomNav = remember(portfolioDig) { portfolioDig == null }

  Scaffold(
      modifier = modifier.fillMaxSize(),
      floatingActionButton = {
        MainActionButton(
            show = showBottomNav,
            page = page,
            onActionSelected = onActionSelected,
        )
      },
      floatingActionButtonPosition = FabPosition.End,
      isFloatingActionButtonDocked = true,
      bottomBar = {
        AnimatedVisibility(
            visible = showBottomNav,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
          MainBottomNav(
              modifier = Modifier.fillMaxWidth(),
              pagerState = pagerState,
              allTabs = allTabs,
              page = page,
          )
        }
      },
  ) { pv ->
    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = portfolioDig,
    ) { dig ->
      if (dig == null) {
        MainContent(
            modifier = Modifier.fillMaxSize().padding(pv),
            appName = appName,
            pagerState = pagerState,
            allTabs = allTabs,
            onHomeDig = onHomeDig,
            onPortfolioDig = onPortfolioDig,
        )
      } else {
        PortfolioDigEntry(
            modifier = Modifier.fillMaxSize().padding(pv),
            params = dig,
            onDismiss = onCloseDig,
        )
      }
    }
  }
}
