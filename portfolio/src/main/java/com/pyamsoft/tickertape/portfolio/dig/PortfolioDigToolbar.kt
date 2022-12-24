package com.pyamsoft.tickertape.portfolio.dig

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
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.quote.YFJumpLink
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.asSymbol

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
internal fun PortfolioDigToolbar(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onClose: () -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
) {
  val holding = state.holding
  val ticker = state.ticker
  val section = state.section
  val title = remember(ticker) { ticker.quote?.company?.company ?: ticker.symbol.raw }

  // Hide tabs in options
  val equityType = holding?.type
  val allTabs =
      remember(equityType) {
        PortfolioDigSections.values().filter { v ->
          if (equityType == null) {
            return@filter !HIDE_TABS_FOR_OPTIONS.contains(v)
          } else {
            return@filter when (equityType) {
              EquityType.OPTION -> !HIDE_TABS_FOR_OPTIONS.contains(v)
              EquityType.CRYPTOCURRENCY -> !HIDE_TABS_FOR_CRYPTO.contains(v)
              else -> true
            }
          }
        }
      }
  val selectedTab = remember(section, allTabs) { allTabs.indexOf(section) }

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
                symbol = state.ticker.symbol,
            )
          },
      )

      ScrollableTabRow(
          backgroundColor = Color.Transparent,
          selectedTabIndex = selectedTab,
      ) {
        // If we use forEach here, compose compiler gives a ClassCastException
        for (tab in allTabs) {
          PortfolioTab(
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
private fun PortfolioTab(
    tab: PortfolioDigSections,
    current: PortfolioDigSections,
    onTabUpdated: (PortfolioDigSections) -> Unit,
) {
  val isSelected = remember(tab, current) { tab == current }
  Tab(
      selected = isSelected,
      onClick = { onTabUpdated(tab) },
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
private fun PreviewPortfolioDigToolbar() {
  val symbol = "MSFT".asSymbol()
  PortfolioDigToolbar(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
          ),
      onClose = {},
      onTabUpdated = {},
  )
}
