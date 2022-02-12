package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.pyamsoft.tickertape.portfolio.dig.chart.PortfolioChart
import com.pyamsoft.tickertape.portfolio.dig.news.PositionNews
import com.pyamsoft.tickertape.portfolio.dig.position.PositionScreen
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
              modifier = Modifier.fillMaxWidth(),
              state = state,
              imageLoader = imageLoader,
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
    imageLoader: ImageLoader,
    currentPrice: StockMoneyValue?,
    onScrub: (Chart.Data?) -> Unit,
    onRangeSelected: (StockChart.IntervalRange) -> Unit,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
) {
  val section = state.section

  Crossfade(
      modifier = modifier,
      targetState = section,
  ) { s ->
    return@Crossfade when (s) {
      PortfolioDigSections.CHART -> {
        PortfolioChart(
            // Chart will size itself
            modifier = Modifier.fillMaxWidth(),
            state = state,
            imageLoader = imageLoader,
            onScrub = onScrub,
            onRangeSelected = onRangeSelected,
        )
      }
      PortfolioDigSections.NEWS -> {
        PositionNews(
            // At most this is slightly larger than half the screen in height
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6F),
            state = state,
            onRefresh = onRefresh,
        )
      }
      PortfolioDigSections.POSITIONS -> {
        PositionScreen(
            // At most this is slightly larger than half the screen in height
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6F),
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
      modifier = modifier.padding(MaterialTheme.keylines.content),
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
  )
}
