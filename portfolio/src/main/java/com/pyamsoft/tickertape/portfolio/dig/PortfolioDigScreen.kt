package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.chart.PortfolioChart
import com.pyamsoft.tickertape.portfolio.dig.news.PositionNews
import com.pyamsoft.tickertape.portfolio.dig.position.PositionScreen
import com.pyamsoft.tickertape.portfolio.dig.splits.SplitScreen
import com.pyamsoft.tickertape.portfolio.dig.stats.PositionStats
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader

@Composable
@JvmOverloads
fun PortfolioDigScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    imageLoader: ImageLoader,
    currentPrice: StockMoneyValue?,
    onClose: () -> Unit,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
    onUpdatePosition: (DbPosition) -> Unit,
    onAddSplit: () -> Unit,
    onDeleteSplit: (DbSplit) -> Unit,
    onUpdateSplit: (DbSplit) -> Unit,
) {
  val isLoading = state.isLoading

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
      PortfolioDigToolbar(
          state = state,
          onClose = onClose,
          onTabUpdated = onTabUpdated,
      )

      Crossfade(
          targetState = isLoading,
      ) { loading ->
        if (loading) {
          Loading(
              modifier = Modifier.fillMaxWidth(),
          )
        } else {
          Content(
              modifier = Modifier.fillMaxSize(),
              state = state,
              imageLoader = imageLoader,
              currentPrice = currentPrice,
              onScrub = onScrub,
              onRangeSelected = onRangeSelected,
              onRefresh = onRefresh,
              onAddPosition = onAddPosition,
              onDeletePosition = onDeletePosition,
              onUpdatePosition = onUpdatePosition,
              onAddSplit = onAddSplit,
              onDeleteSplit = onDeleteSplit,
              onUpdateSplit = onUpdateSplit,
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
    imageLoader: ImageLoader,
    currentPrice: StockMoneyValue?,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
    onUpdatePosition: (DbPosition) -> Unit,
    onAddSplit: () -> Unit,
    onDeleteSplit: (DbSplit) -> Unit,
    onUpdateSplit: (DbSplit) -> Unit,
) {
  val section = state.section

  Crossfade(
      modifier = modifier.fillMaxWidth(),
      targetState = section,
  ) { s ->
    return@Crossfade when (s) {
      PortfolioDigSections.CHART -> {
        PortfolioChart(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onScrub = onScrub,
            onRangeSelected = onRangeSelected,
        )
      }
      PortfolioDigSections.NEWS -> {
        PositionNews(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
        )
      }
      PortfolioDigSections.POSITIONS -> {
        PositionScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onAddPosition = onAddPosition,
            currentPrice = currentPrice,
            onDeletePosition = onDeletePosition,
            onUpdatePosition = onUpdatePosition,
        )
      }
      PortfolioDigSections.STATISTICS -> {
        PositionStats(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
        )
      }
      PortfolioDigSections.SPLITS -> {
        SplitScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onAddSplit = onAddSplit,
            onDeleteSplit = onDeleteSplit,
            onUpdateSplit = onUpdateSplit,
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
      modifier = modifier.padding(MaterialTheme.keylines.content),
      contentAlignment = Alignment.Center,
  ) { CircularProgressIndicator() }
}

@Preview
@Composable
private fun PreviewPortfolioDigScreen() {
  val symbol = "MSFT".asSymbol()
  PortfolioDigScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
          ),
      imageLoader = createNewTestImageLoader(),
      currentPrice = null,
      onClose = {},
      onScrub = {},
      onRangeSelected = {},
      onTabUpdated = {},
      onRefresh = {},
      onAddPosition = {},
      onDeletePosition = {},
      onUpdatePosition = {},
      onAddSplit = {},
      onDeleteSplit = {},
      onUpdateSplit = {},
  )
}
