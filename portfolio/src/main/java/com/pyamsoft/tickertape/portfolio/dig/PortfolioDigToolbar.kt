package com.pyamsoft.tickertape.portfolio.dig

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.quote.YFJumpLink
import com.pyamsoft.tickertape.quote.isIndex
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
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
    holding: DbHolding?
): SnapshotStateList<PortfolioDigSections> {
  // Hide tabs in options
  val equityType = holding?.type
  return remember(
      equityType,
      symbol,
  ) {
    PortfolioDigSections.values()
        .filter { v ->
          if (equityType == null) {
            // Just provide something so that we have a visual placeholder
            return@filter !HIDE_TABS_FOR_OPTIONS.contains(v)
          } else {
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
        }
        .toMutableStateList()
  }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
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
      shape = RectangleShape,
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
@OptIn(ExperimentalPagerApi::class)
private fun PreviewPortfolioDigToolbar() {
  val symbol = "MSFT".asSymbol()
  val state =
      MutablePortfolioDigViewState(
          symbol = symbol,
      )
  val holding by state.holding.collectAsState()

  PortfolioDigToolbar(
      state = state,
      pagerState = rememberPagerState(),
      allTabs = rememberTabs(symbol, holding),
      onClose = {},
  )
}
