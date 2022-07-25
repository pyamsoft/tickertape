package com.pyamsoft.tickertape.portfolio.dig.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.quote.dig.DigNews
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PositionNews(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  val news = state.news

  DigNews(
      modifier = modifier,
      isLoading = isLoading,
      news = news,
      onRefresh = onRefresh,
  )
}

@Preview
@Composable
private fun PreviewPositionNews() {
  val symbol = "MSFT".asSymbol()
  PositionNews(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
          ),
      onRefresh = {},
  )
}
