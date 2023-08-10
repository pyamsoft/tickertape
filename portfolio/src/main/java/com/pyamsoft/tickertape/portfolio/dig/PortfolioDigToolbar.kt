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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.annotation.CheckResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.quote.YFJumpLink
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.isIndex
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.test.TestClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val HIDE_TABS_FOR_INDEXES =
    arrayOf(
        PortfolioDigSections.OPTIONS_CHAIN,
        PortfolioDigSections.STATISTICS,
        PortfolioDigSections.SPLITS,
    )

private val HIDE_TABS_FOR_OPTIONS =
    arrayOf(
        PortfolioDigSections.OPTIONS_CHAIN,
    )

private val HIDE_TABS_FOR_CRYPTO =
    arrayOf(
        PortfolioDigSections.OPTIONS_CHAIN,
        PortfolioDigSections.STATISTICS,
        PortfolioDigSections.SPLITS,
    )

@Composable
@CheckResult
internal fun rememberTabs(
    symbol: StockSymbol,
    holding: Maybe<out DbHolding>?
): SnapshotStateList<PortfolioDigSections> {
  // Hide tabs in options
  return remember(
      holding,
      symbol,
  ) {
    if (holding == null) {
      return@remember mutableStateListOf()
    } else {
      return@remember PortfolioDigSections.values()
          .asSequence()
          .filterNot { PortfolioDigSections.PRICE_ALERTS == it }
          .filterNot { PortfolioDigSections.OPTIONS_CHAIN == it }
          .filter { v ->
            // Kotlin is weird sometimes
            when (val h: Maybe<out DbHolding> = holding.requireNotNull()) {
              is Maybe.Data -> {
                val equityType = h.data.type
                return@filter if (symbol.isIndex()) {
                  !HIDE_TABS_FOR_INDEXES.contains(v)
                } else {
                  when (equityType) {
                    EquityType.OPTION -> !HIDE_TABS_FOR_OPTIONS.contains(v)
                    EquityType.CRYPTOCURRENCY -> !HIDE_TABS_FOR_CRYPTO.contains(v)
                    else -> true
                  }
                }
              }
              is Maybe.None -> {
                return@filter !HIDE_TABS_FOR_INDEXES.contains(v) &&
                    !HIDE_TABS_FOR_CRYPTO.contains(v) &&
                    !HIDE_TABS_FOR_OPTIONS.contains(v)
              }
            }
          }
          .toList()
          .toMutableStateList()
    }
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
internal fun PortfolioDigToolbar(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    pagerState: PagerState,
    allTabs: SnapshotStateList<PortfolioDigSections>,
    onClose: () -> Unit,
) {
  val ticker by state.ticker.collectAsState()

  val title = remember(ticker) { ticker.quote?.company?.company ?: ticker.symbol.raw }

  Surface(
      modifier = modifier,
      elevation = ZeroElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape =
          MaterialTheme.shapes.medium.copy(
              bottomEnd = ZeroCornerSize,
              bottomStart = ZeroCornerSize,
          ),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      TopAppBar(
          modifier = Modifier.statusBarsPadding().fillMaxWidth(),
          backgroundColor = Color.Transparent,
          elevation = ZeroElevation,
          title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          },
          navigationIcon = {
            IconButton(
                onClick = onClose,
            ) {
              Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Close",
              )
            }
          },
          actions = {
            YFJumpLink(
                symbol = ticker.symbol,
            )
          },
      )

      if (allTabs.isNotEmpty()) {
        val currentPage = pagerState.currentPage
        ScrollableTabRow(
            backgroundColor = Color.Transparent,
            selectedTabIndex = currentPage,
            indicator = { tabPositions ->
              TabRowDefaults.Indicator(
                  modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
              )
            },
        ) {
          val scope = rememberCoroutineScope()
          for (index in allTabs.indices) {
            val tab = allTabs[index]
            val isSelected =
                remember(
                    index,
                    currentPage,
                ) {
                  index == currentPage
                }

            PortfolioTab(
                tab = tab,
                isSelected = isSelected,
                onSelected = {
                  // Click fires the index to update
                  // The index updating is caught by the snapshot flow
                  // Which then triggers the page update function
                  scope.launch(context = Dispatchers.Main) { pagerState.animateScrollToPage(index) }
                },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PortfolioTab(
    modifier: Modifier = Modifier,
    tab: PortfolioDigSections,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
  Tab(
      modifier = modifier,
      selected = isSelected,
      onClick = onSelected,
  ) {
    Text(
        modifier =
            Modifier.padding(vertical = MaterialTheme.keylines.typography)
                .padding(horizontal = MaterialTheme.keylines.baseline),
        text = tab.display,
    )
  }
}

@Preview
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PreviewPortfolioDigToolbar() {
  val symbol = TestSymbol
  val clock = TestClock

  val state =
      MutablePortfolioDigViewState(
          params =
              PortfolioDigParams(
                  symbol = symbol,
                  equityType = EquityType.STOCK,
                  lookupSymbol = null,
              ),
          clock = clock,
      )
  val holding by state.holding.collectAsState()
  val allTabs = rememberTabs(symbol, holding)

  PortfolioDigToolbar(
      state = state,
      pagerState =
          rememberPagerState(
              initialPage = 0,
              initialPageOffsetFraction = 0F,
              pageCount = { allTabs.size },
          ),
      allTabs = allTabs,
      onClose = {},
  )
}
