package com.pyamsoft.tickertape.home.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import com.pyamsoft.tickertape.portfolio.item.PorfolioSummaryItem

@Composable
@JvmOverloads
fun HomePortfolioSummaryItem(
    modifier: Modifier = Modifier,
    portfolio: PortfolioStockList,
) {
  PorfolioSummaryItem(
      modifier = modifier,
      portfolio = portfolio,
  )
}

@Preview
@Composable
private fun PreviewHomePortfolioSummaryItem() {
  Surface {
    HomePortfolioSummaryItem(
        portfolio = PortfolioStockList.empty(),
    )
  }
}
