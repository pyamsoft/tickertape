/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber

internal class MainInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: MainViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun WatchTabSwipe(
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
) {
  // Watch for a swipe causing a page change and update accordingly
  LaunchedEffect(
      pagerState,
      allTabs,
  ) {
    snapshotFlow { pagerState.targetPage }
        .distinctUntilChanged()
        .mapNotNull { allTabs.getOrNull(it) }
        .collect { page -> Timber.d("Page swiped: $page") }
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MountHooks(
    viewModel: MainViewModeler,
    pagerState: PagerState,
    allTabs: SnapshotStateList<MainPage>,
) {
  SaveStateDisposableEffect(viewModel)

  WatchTabSwipe(
      pagerState = pagerState,
      allTabs = allTabs,
  )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun MainEntry(
    modifier: Modifier = Modifier,
    appName: String,
) {
  val component = rememberComposableInjector { MainInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  val allTabs = rememberAllTabs()
  val pagerState =
      rememberPagerState(
          initialPage = 0,
          initialPageOffsetFraction = 0F,
          pageCount = { allTabs.size },
      )
  val scope = rememberCoroutineScope()

  MountHooks(
      viewModel = viewModel,
      pagerState = pagerState,
      allTabs = allTabs,
  )

  MainScreen(
      modifier = modifier,
      appName = appName,
      state = viewModel,
      pagerState = pagerState,
      allTabs = allTabs,
      onActionSelected = {
        viewModel.handleMainActionSelected(
            scope = scope,
            page = it,
        )
      },
      onHomeDig = { viewModel.handleOpenDig(it) },
      onPortfolioDig = { stock ->
        val quote = stock.ticker?.quote
        val lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol else quote?.symbol
        viewModel.handleOpenDig(
            holding = stock.holding,
            lookupSymbol = lookupSymbol,
            currentPrice = quote?.currentSession?.price,
        )
      },
      onCloseDig = { viewModel.handleCloseDig() },
  )
}
