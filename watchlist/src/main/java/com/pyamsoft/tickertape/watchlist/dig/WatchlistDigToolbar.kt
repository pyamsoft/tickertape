package com.pyamsoft.tickertape.watchlist.dig

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.AppBarDefaults
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.YFJumpLink
import com.pyamsoft.tickertape.quote.isIndex
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.icon.StarBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val HIDE_TABS_FOR_INDEXES =
    arrayOf(
        WatchlistDigSections.OPTIONS_CHAIN,
        WatchlistDigSections.STATISTICS,
    )

private val HIDE_TABS_FOR_OPTIONS =
    arrayOf(
        WatchlistDigSections.OPTIONS_CHAIN,
    )

private val HIDE_TABS_FOR_CRYPTO =
    arrayOf(
        WatchlistDigSections.OPTIONS_CHAIN,
        WatchlistDigSections.STATISTICS,
    )

@Composable
@CheckResult
internal fun rememberTabs(ticker: Ticker): SnapshotStateList<WatchlistDigSections> {
  val symbol = ticker.symbol
  val equityType = ticker.quote?.type

  // Hide tabs in options
  return remember(
      equityType,
      symbol,
  ) {
    val list =
        WatchlistDigSections.values().filter { v ->
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

    return@remember mutableStateListOf(*list.toTypedArray())
  }
}

@Composable
@OptIn(ExperimentalPagerApi::class)
internal fun WatchlistDigToolbar(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    pagerState: PagerState,
    allTabs: SnapshotStateList<WatchlistDigSections>,
    onClose: () -> Unit,
    onModifyWatchlist: () -> Unit,
) {
  val ticker by state.ticker.collectAsState()
  val watchlistStatus by state.watchlistStatus.collectAsState()

  val title = remember(ticker) { ticker.quote?.company?.company ?: ticker.symbol.raw }

  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape = RectangleShape,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      TopAppBar(
          modifier = Modifier.fillMaxWidth().statusBarsPadding(),
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
            if (watchlistStatus != WatchlistDigViewState.WatchlistStatus.NONE) {
              val isInWatchlist =
                  remember(watchlistStatus) {
                    watchlistStatus == WatchlistDigViewState.WatchlistStatus.IN_LIST
                  }
              IconButton(
                  onClick = onModifyWatchlist,
              ) {
                Icon(
                    imageVector = if (isInWatchlist) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription =
                        "${if (isInWatchlist) "Add to" else "Remove from"} Watchlist",
                )
              }
            }

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

          WatchlistTab(
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
private fun WatchlistTab(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    tab: WatchlistDigSections,
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
private fun PreviewWatchlistDigToolbar() {
  val symbol = "MSFT".asSymbol()
  val state =
      MutableWatchlistDigViewState(
          symbol = symbol,
      )
  val ticker by state.ticker.collectAsState()
  WatchlistDigToolbar(
      state = state,
      pagerState = rememberPagerState(),
      allTabs = rememberTabs(ticker),
      onClose = {},
      onModifyWatchlist = {},
  )
}
