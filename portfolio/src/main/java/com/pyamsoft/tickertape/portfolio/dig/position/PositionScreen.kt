package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionScreen
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun PositionScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    currentPrice: StockMoneyValue?,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
    onUpdatePosition: (DbPosition) -> Unit,
) {
  val tradeSide = state.tradeSide
  val equityType = state.equityType
  val isLoading = state.isLoading
  val positions = state.positions
  val splits = state.stockSplits

  val positionError = state.positionsError
  val holdingError = state.holdingError

  val isAddVisible =
      remember(isLoading, positionError, holdingError) {
        !isLoading && positionError == null && holdingError == null
      }

  BasePositionScreen(
      modifier = modifier,
      label = "Add Position",
      isAddVisible = isAddVisible,
      items = positions,
      isLoading = isLoading,
      onRefresh = onRefresh,
      onAddClicked = onAddPosition,
      itemKey = { it.id.raw },
      renderListItem = { position ->
        PositionItem(
            modifier =
                Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = { onUpdatePosition(position) },
                        onLongClick = { onDeletePosition(position) },
                    ),
            position = position,
            currentPrice = currentPrice,
            equityType = equityType,
            tradeSide = tradeSide,
            splits = splits,
        )
      },
  )
}

@Preview
@Composable
private fun PreviewPositionScreen() {
  val symbol = "MSFT".asSymbol()
  Surface {
    PositionScreen(
        modifier = Modifier.fillMaxSize(),
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
        onUpdatePosition = {},
    )
  }
}
