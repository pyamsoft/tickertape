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
import com.pyamsoft.tickertape.portfolio.dig.position.PositionScreen
import com.pyamsoft.tickertape.portfolio.dig.splits.SplitScreen
import com.pyamsoft.tickertape.quote.Chart
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.statistics.DigKeyStatistics
import com.pyamsoft.tickertape.quote.dig.DigNews
import com.pyamsoft.tickertape.quote.dig.DigOptionsChain
import com.pyamsoft.tickertape.quote.dig.DigRecommendations
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import java.time.LocalDate

@Composable
@JvmOverloads
fun PortfolioDigScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    imageLoader: ImageLoader,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onTabUpdated: (PortfolioDigSections) -> Unit,
    onChartScrub: (Chart.Data?) -> Unit,
    onChartRangeSelected: (StockChart.IntervalRange) -> Unit,
    onPositionAdd: () -> Unit,
    onPositionDelete: (DbPosition) -> Unit,
    onPositionUpdate: (DbPosition) -> Unit,
    onSplitAdd: () -> Unit,
    onSplitDeleted: (DbSplit) -> Unit,
    onSplitUpdated: (DbSplit) -> Unit,
    onRecClick: (Ticker) -> Unit,
    onOptionSectionChanged: (StockOptions.Contract.Type) -> Unit,
    onOptionExpirationDateChanged: (LocalDate) -> Unit,
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
              onChartScrub = onChartScrub,
              onChartRangeSelected = onChartRangeSelected,
              onRefresh = onRefresh,
              onPositionAdd = onPositionAdd,
              onPositionDelete = onPositionDelete,
              onPositionUpdate = onPositionUpdate,
              onSplitAdd = onSplitAdd,
              onSplitDeleted = onSplitDeleted,
              onSplitUpdated = onSplitUpdated,
              onRecClick = onRecClick,
              onOptionSectionChanged = onOptionSectionChanged,
              onOptionExpirationDateChanged = onOptionExpirationDateChanged,
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
    onRefresh: () -> Unit,
    onChartScrub: (Chart.Data?) -> Unit,
    onChartRangeSelected: (StockChart.IntervalRange) -> Unit,
    onPositionAdd: () -> Unit,
    onPositionDelete: (DbPosition) -> Unit,
    onPositionUpdate: (DbPosition) -> Unit,
    onSplitAdd: () -> Unit,
    onSplitDeleted: (DbSplit) -> Unit,
    onSplitUpdated: (DbSplit) -> Unit,
    onRecClick: (Ticker) -> Unit,
    onOptionSectionChanged: (StockOptions.Contract.Type) -> Unit,
    onOptionExpirationDateChanged: (LocalDate) -> Unit,
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
            onScrub = onChartScrub,
            onRangeSelected = onChartRangeSelected,
        )
      }
      PortfolioDigSections.NEWS -> {
        DigNews(
            modifier = Modifier.fillMaxSize(),
            state = state,
            imageLoader = imageLoader,
            onRefresh = onRefresh,
        )
      }
      PortfolioDigSections.POSITIONS -> {
        PositionScreen(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onAddPosition = onPositionAdd,
            onDeletePosition = onPositionDelete,
            onUpdatePosition = onPositionUpdate,
        )
      }
      PortfolioDigSections.STATISTICS -> {
        DigKeyStatistics(
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
            onAddSplit = onSplitAdd,
            onDeleteSplit = onSplitDeleted,
            onUpdateSplit = onSplitUpdated,
        )
      }
      PortfolioDigSections.RECOMMENDATIONS -> {
        DigRecommendations(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onRecClick = onRecClick,
        )
      }
      PortfolioDigSections.OPTIONS_CHAIN -> {
        DigOptionsChain(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onRefresh = onRefresh,
            onSectionChanged = onOptionSectionChanged,
            onExpirationDateChanged = onOptionExpirationDateChanged,
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
  ) {
    CircularProgressIndicator()
  }
}

@Preview
@Composable
private fun PreviewPortfolioDigScreen() {
  val symbol = "MSFT".asSymbol()
  PortfolioDigScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
          ),
      imageLoader = createNewTestImageLoader(),
      onClose = {},
      onChartScrub = {},
      onChartRangeSelected = {},
      onTabUpdated = {},
      onRefresh = {},
      onPositionAdd = {},
      onPositionDelete = {},
      onPositionUpdate = {},
      onSplitAdd = {},
      onSplitDeleted = {},
      onSplitUpdated = {},
      onRecClick = {},
      onOptionExpirationDateChanged = {},
      onOptionSectionChanged = {},
  )
}
