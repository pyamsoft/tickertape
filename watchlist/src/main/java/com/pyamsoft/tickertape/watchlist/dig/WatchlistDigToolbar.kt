package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.icon.StarBorder

private val HIDE_TABS_FOR_OPTIONS =
    arrayOf(
        WatchlistDigSections.OPTIONS_CHAIN,
        WatchlistDigSections.NEWS,
        WatchlistDigSections.RECOMMENDATIONS,
        WatchlistDigSections.STATISTICS,
    )

@Composable
internal fun WatchlistDigToolbar(
    modifier: Modifier = Modifier,
    state: WatchlistDigViewState,
    onClose: () -> Unit,
    onModifyWatchlist: () -> Unit,
    onTabUpdated: (WatchlistDigSections) -> Unit,
) {
  val isLoading = state.isLoading
  val ticker = state.ticker
  val isInWatchlist = state.isInWatchlist
  val section = state.section
  val title = remember(ticker) { ticker.quote?.company?.company ?: ticker.symbol.raw }
  val isInWatchlistError = state.isInWatchlistError
  val hasIsInWatchlistError = remember(isInWatchlistError) { isInWatchlistError != null }

  // Hide tabs in options
  val allTabs =
      remember(ticker.quote) {
        WatchlistDigSections.values().filter { v ->
          val q = ticker.quote
          if (q == null) {
            return@filter !HIDE_TABS_FOR_OPTIONS.contains(v)
          } else {
            if (q.type == EquityType.OPTION) {
              return@filter !HIDE_TABS_FOR_OPTIONS.contains(v)
            } else {
              return@filter true
            }
          }
        }
      }

  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape =
          MaterialTheme.shapes.medium.copy(
              topEnd = ZeroCornerSize,
              topStart = ZeroCornerSize,
          ),
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
            if (!isLoading && !hasIsInWatchlistError) {
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
          },
      )

      ScrollableTabRow(
          backgroundColor = Color.Transparent,
          selectedTabIndex = section.ordinal,
      ) {
        // If we use forEach here, compose compiler gives a ClassCastException
        for (tab in allTabs) {
          WatchlistTab(
              current = section,
              tab = tab,
              onTabUpdated = onTabUpdated,
          )
        }
      }
    }
  }
}

@Composable
private fun WatchlistTab(
    modifier: Modifier = Modifier,
    tab: WatchlistDigSections,
    current: WatchlistDigSections,
    onTabUpdated: (WatchlistDigSections) -> Unit,
) {
  val isSelected = remember(tab, current) { tab == current }

  Tab(
      modifier = modifier,
      selected = isSelected,
      onClick = { onTabUpdated(tab) },
  ) {
    Text(
        modifier = Modifier.padding(vertical = MaterialTheme.keylines.typography),
        text = tab.display,
    )
  }
}

@Preview
@Composable
private fun PreviewWatchlistDigToolbar() {
  val symbol = "MSFT".asSymbol()
  WatchlistDigToolbar(
      state =
          MutableWatchlistDigViewState(
              symbol = symbol,
          ),
      onClose = {},
      onModifyWatchlist = {},
      onTabUpdated = {},
  )
}
