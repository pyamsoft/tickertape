package com.pyamsoft.tickertape.portfolio

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.tickertape.portfolio.item.PorfolioSummaryItem
import com.pyamsoft.tickertape.portfolio.item.PortfolioItem
import com.pyamsoft.tickertape.quote.SearchBar
import com.pyamsoft.tickertape.quote.add.NewTickerFab
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.FabDefaults

@Composable
@JvmOverloads
fun PortfolioScreen(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
    navBarBottomHeight: Int = 0,
    onRefresh: () -> Unit,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onFabClick: () -> Unit,
) {
  val loading = state.isLoading
  val scaffoldState = rememberScaffoldState()

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
  ) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = loading),
        onRefresh = onRefresh,
    ) {
      Content(
          modifier = Modifier.fillMaxSize(),
          state = state,
          navBarBottomHeight = navBarBottomHeight,
          onRefresh = onRefresh,
          onSelect = onSelect,
          onDelete = onDelete,
          onSearchChanged = onSearchChanged,
          onTabUpdated = onTabUpdated,
          onFabClick = onFabClick,
      )
    }
  }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
    navBarBottomHeight: Int,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
    onSearchChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onFabClick: () -> Unit,
) {
  val error = state.error
  val portfolio = state.portfolio
  val stocks = state.stocks
  val search = state.query
  val tab = state.section
  val isLoading = state.isLoading

  val density = LocalDensity.current
  val bottomPaddingDp =
      remember(density, navBarBottomHeight) { density.run { navBarBottomHeight.toDp() } }
  val fabBottomPadding = remember(bottomPaddingDp) { bottomPaddingDp + 16.dp }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomCenter,
  ) {
    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = error,
    ) { err ->
      if (err == null) {
        Portfolio(
            modifier = Modifier.fillMaxSize(),
            portfolio = portfolio,
            stocks = stocks,
            navBarBottomHeight = bottomPaddingDp,
            search = search,
            tab = tab,
            onSelect = onSelect,
            onDelete = onDelete,
            onSearchChanged = onSearchChanged,
            onTabUpdated = onTabUpdated,
        )
      } else {
        Error(
            modifier = Modifier.fillMaxSize(),
            error = err,
            onRefresh = onRefresh,
        )
      }
    }

    NewTickerFab(
        visible = !isLoading,
        modifier =
            Modifier.padding(16.dp)
                .navigationBarsPadding(bottom = true)
                .padding(bottom = fabBottomPadding),
        onClick = onFabClick,
    )
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Portfolio(
    modifier: Modifier = Modifier,
    portfolio: PortfolioStockList,
    stocks: List<PortfolioStock>,
    search: String,
    navBarBottomHeight: Dp,
    tab: EquityType,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
) {
  val portfolioTickers = remember(stocks) { stocks.filter { it.ticker != null } }

  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(horizontal = 8.dp),
  ) {
    if (!portfolio.isEmpty) {
      item {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
          Spacer(
              modifier = Modifier.statusBarsHeight(),
          )
          PorfolioSummaryItem(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
              portfolio = portfolio,
          )
        }
      }
    }

    stickyHeader {
      Column(
          modifier = Modifier.fillMaxWidth(),
      ) {
        Spacer(
            modifier = Modifier.statusBarsHeight(),
        )
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            search = search,
            currentTab = tab,
            onSearchChanged = onSearchChanged,
            onTabUpdated = onTabUpdated,
        )
      }
    }

    itemsIndexed(
        items = portfolioTickers,
        key = { _, item -> item.holding.symbol().symbol() },
    ) { index, ps ->
      if (index == 0) {
        Spacer(
            modifier = Modifier.height(16.dp),
        )
      }

      PortfolioItem(
          modifier = Modifier.fillMaxWidth(),
          stock = ps,
          onSelect = onSelect,
          onDelete = onDelete,
      )

      Spacer(
          modifier = Modifier.height(16.dp),
      )
    }

    item {
      Spacer(
          modifier =
              Modifier.navigationBarsHeight(
                  additional = navBarBottomHeight + FabDefaults.FAB_OFFSET_DP.dp,
              ),
      )
    }
  }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier,
    error: Throwable,
    onRefresh: () -> Unit,
) {
  Column(
      modifier = modifier.padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        textAlign = TextAlign.Center,
        text = error.message ?: "An unexpected error occurred",
        style =
            MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.error,
            ),
    )

    Text(
        modifier = Modifier.padding(top = 16.dp),
        textAlign = TextAlign.Center,
        text = "Please try again later.",
        style = MaterialTheme.typography.body2,
    )

    Button(
        modifier = Modifier.padding(top = 16.dp),
        onClick = onRefresh,
    ) {
      Text(
          text = "Refresh",
      )
    }
  }
}

@Preview
@Composable
private fun PreviewPortfolioScreen() {
  PortfolioScreen(
      state = MutablePortfolioViewState(),
      onRefresh = {},
      onDelete = {},
      onSelect = {},
      onSearchChanged = {},
      onTabUpdated = {},
      onFabClick = {},
  )
}
