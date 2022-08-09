package com.pyamsoft.tickertape.portfolio.dig.recs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigRecommendations
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
internal fun PositionRecommendations(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
    onRecClick: (Ticker) -> Unit,
) {
  val isLoading = state.isLoading
  val recs = state.recommendations

  DigRecommendations(
      modifier = modifier,
      isLoading = isLoading,
      recommendations = recs,
      onRefresh = onRefresh,
      onRecClick = onRecClick,
  )
}

@Preview
@Composable
private fun PreviewPositionRecommendations() {
  val symbol = "MSFT".asSymbol()
  PositionRecommendations(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
          ),
      onRefresh = {},
      onRecClick = {},
  )
}
