package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun PortfolioDigToolbar(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onClose: () -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
) {
  val ticker = state.ticker
  val section = state.section
  val title = remember(ticker) { ticker.quote?.company()?.company() ?: ticker.symbol.symbol() }
  val allTabs = remember { PortfolioDigSections.values() }

  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
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
      val contentColor = LocalContentColor.current

      TopAppBar(
          modifier = Modifier.fillMaxWidth(),
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
      )

      ScrollableTabRow(
          backgroundColor = Color.Transparent,
          selectedTabIndex = section.ordinal,
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
        modifier = Modifier.padding(vertical = MaterialTheme.keylines.typography),
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
              lookupSymbol = symbol,
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
          ),
      onClose = {},
      onTabUpdated = {},
  )
}
