package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.chart.PorfolioChart
import com.pyamsoft.tickertape.portfolio.dig.position.PositionScreen
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@JvmOverloads
fun PortfolioDigScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    currentPrice: StockMoneyValue?,
    onClose: () -> Unit,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
) {
  val ticker = state.ticker
  val isLoading = state.isLoading
  val section = state.section

  Surface(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      PortfolioDigToolbar(
          ticker = ticker,
          section = section,
          onClose = onClose,
          onTabUpdated = onTabUpdated,
      )

      Crossfade(
          modifier = modifier,
          targetState = isLoading,
      ) { loading ->
        if (loading) {
          Loading(
              modifier = Modifier.fillMaxWidth(),
          )
        } else {
          Content(
              modifier = Modifier.fillMaxWidth(),
              state = state,
              currentPrice = currentPrice,
              onScrub = onScrub,
              onRangeSelected = onRangeSelected,
              onRefresh = onRefresh,
              onAddPosition = onAddPosition,
              onDeletePosition = onDeletePosition,
          )
        }
      }
    }
  }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    currentPrice: StockMoneyValue?,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
) {
  Crossfade(
      modifier = modifier,
      targetState = state.section,
  ) { section ->
    return@Crossfade when (section) {
      PortfolioDigSections.CHART -> {
        PorfolioChart(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            onScrub = onScrub,
            onRangeSelected = onRangeSelected,
        )
      }
      PortfolioDigSections.POSITIONS -> {
        PositionScreen(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75F),
            state = state,
            onRefresh = onRefresh,
            onAddPosition = onAddPosition,
            currentPrice = currentPrice,
            onDeletePosition = onDeletePosition,
        )
      }
    }
  }
}

@Composable
private fun Loading(
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier.padding(16.dp),
      contentAlignment = Alignment.Center,
  ) { CircularProgressIndicator() }
}

@Preview
@Composable
private fun PreviewPortfolioDigScreen() {
  PortfolioDigScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = "MSFT".asSymbol(),
          ),
      currentPrice = null,
      onClose = {},
      onScrub = {},
      onRangeSelected = {},
      onTabUpdated = {},
      onRefresh = {},
      onAddPosition = {},
      onDeletePosition = {},
  )
}
