package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol
import timber.log.Timber

private const val FAB_OFFSET = 56 + 16

@Composable
@JvmOverloads
internal fun PositionScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    currentPrice: StockMoneyValue?,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
) {
  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    PositionsList(
        modifier = Modifier.matchParentSize(),
        state = state,
        currentPrice = currentPrice,
        onRefresh = onRefresh,
        onDeletePosition = onDeletePosition,
    )

    PositionsAdd(
        state = state,
        onAddPosition = onAddPosition,
    )
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PositionsList(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    currentPrice: StockMoneyValue?,
    onRefresh: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
) {
  val tradeSide = state.tradeSide
  val equityType = state.equityType
  val isLoading = state.isLoading
  val positions = state.positions
  SwipeRefresh(
      modifier = modifier,
      state = rememberSwipeRefreshState(isRefreshing = isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn(
        contentPadding = PaddingValues(MaterialTheme.keylines.content),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
    ) {
      items(
          items = positions,
          key = { it.id().id },
      ) { item ->
        PositionItem(
            modifier =
                Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = { Timber.d("Click on item: $item") },
                        onLongClick = { onDeletePosition(item) },
                    ),
            position = item,
            currentPrice = currentPrice,
            equityType = equityType,
            tradeSide = tradeSide,
        )
      }

      item {
        Spacer(
            modifier = Modifier.height(FAB_OFFSET.dp),
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun PositionsAdd(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onAddPosition: () -> Unit,
) {
  val isLoading = state.isLoading
  val positionError = state.positionsError
  val holdingError = state.holdingError

  val isAddVisible =
      remember(isLoading, positionError, holdingError) {
        !isLoading && positionError == null && holdingError == null
      }

  AnimatedVisibility(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      visible = isAddVisible,
  ) {
    Box {
      FloatingActionButton(
          onClick = onAddPosition,
      ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Position",
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewPositionScreen() {
  val symbol = "MSFT".asSymbol()
  PositionScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = symbol,
              lookupSymbol = symbol,
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
          ),
      currentPrice = null,
      onAddPosition = {},
      onRefresh = {},
      onDeletePosition = {},
  )
}
