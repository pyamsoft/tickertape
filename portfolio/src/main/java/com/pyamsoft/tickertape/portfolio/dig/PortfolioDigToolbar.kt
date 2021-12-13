package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
internal fun PortfolioDigToolbar(
    modifier: Modifier = Modifier,
    section: PortfolioDigSections,
    ticker: Ticker,
    onClose: () -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
) {
  val title = ticker.quote?.company()?.company() ?: ticker.symbol.symbol()

  Surface(
      modifier = modifier,
      elevation = AppBarDefaults.TopAppBarElevation,
      contentColor = Color.White,
      color = MaterialTheme.colors.primary,
      shape = MaterialTheme.shapes.medium,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      val contentColor = LocalContentColor.current

      TopAppBar(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.Transparent,
          contentColor = contentColor,
          elevation = 0.dp,
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

      TabRow(
          backgroundColor = Color.Transparent,
          selectedTabIndex = section.ordinal,
      ) {
        // If we use forEach here, compose compiler gives a ClassCastException
        for (tab in PortfolioDigSections.values()) {
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
  Tab(
      selected = tab == current,
      onClick = { onTabUpdated(tab) },
  ) {
    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = tab.display,
    )
  }
}

@Preview
@Composable
private fun PreviewPortfolioDigToolbar() {
  val symbol = "MSFT".asSymbol()
  PortfolioDigToolbar(
      ticker =
          Ticker(
              symbol = symbol,
              quote = newTestQuote(symbol),
              chart = null,
          ),
      section = PortfolioDigSections.POSITIONS,
      onClose = {},
      onTabUpdated = {},
  )
}
