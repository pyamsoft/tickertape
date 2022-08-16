package com.pyamsoft.tickertape.watchlist.dig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
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
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.icon.StarBorder

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
  val isAllowedToModifyWatchlist = state.isAllowModifyWatchlist
  val section = state.section
  val title = remember(ticker) { ticker.quote?.company?.company ?: ticker.symbol.raw }
  val isInWatchlistError = state.isInWatchlistError
  val hasIsInWatchlistError = remember(isInWatchlistError) { isInWatchlistError != null }
  val allTabs = remember { WatchlistDigSections.values() }

  val contentColor = LocalContentColor.current

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
          contentColor = contentColor,
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
            if (!isLoading && isAllowedToModifyWatchlist && !hasIsInWatchlistError) {
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
              lookupSymbol = symbol,
              allowModifyWatchlist = true,
          ),
      onClose = {},
      onModifyWatchlist = {},
      onTabUpdated = {},
  )
}
