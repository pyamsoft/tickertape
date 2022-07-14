package com.pyamsoft.tickertape.portfolio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.google.accompanist.insets.statusBarsHeight
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.portfolio.item.PorfolioSummaryItem
import com.pyamsoft.tickertape.portfolio.item.PortfolioItem
import com.pyamsoft.tickertape.quote.BaseListScreen
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import kotlinx.coroutines.CoroutineScope

@Composable
@JvmOverloads
fun PortfolioScreen(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
    imageLoader: ImageLoader,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onFabClick: () -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
) {
  val isLoading = state.isLoading
  val pageError = state.error
  val stocks = state.stocks
  val search = state.query
  val tab = state.section

  BaseListScreen(
      modifier = modifier,
      navBarBottomHeight = navBarBottomHeight,
      imageLoader = imageLoader,
      isLoading = isLoading,
      pageError = pageError,
      list = stocks,
      search = search,
      tab = tab,
      emptyStateImage = R.drawable.portfolio_empty,
      emptyStateLabel = "Nothing in your portfolio, add a position!",
      onRefresh = onRefresh,
      onSearchChanged = onSearchChanged,
      onTabUpdated = onTabUpdated,
      onFabClick = onFabClick,
      onRegenerateList = onRegenerateList,
      itemKey = { index, stock -> "${stock.holding.symbol().symbol()}-${index}" },
      renderHeader = {
        PortfolioSummary(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
      },
      renderListItem = { stock ->
        PortfolioItem(
            modifier = Modifier.fillMaxWidth(),
            stock = stock,
            onSelect = onSelect,
            onDelete = onDelete,
        )
      },
  )
}

@Composable
private fun PortfolioSummary(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
) {
  val portfolio = state.portfolio

  Column(
      modifier = modifier,
  ) {
    Spacer(
        modifier = Modifier.statusBarsHeight(),
    )
    PorfolioSummaryItem(
        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.baseline),
        portfolio = portfolio,
    )
  }
}

@Preview
@Composable
private fun PreviewPortfolioScreen() {
  PortfolioScreen(
      state = MutablePortfolioViewState(),
      imageLoader = createNewTestImageLoader(),
      onRefresh = {},
      onDelete = {},
      onSelect = {},
      onSearchChanged = {},
      onTabUpdated = {},
      onFabClick = {},
      onRegenerateList = {},
  )
}
