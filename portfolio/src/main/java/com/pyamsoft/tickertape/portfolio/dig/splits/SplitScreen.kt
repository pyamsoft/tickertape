package com.pyamsoft.tickertape.portfolio.dig.splits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.base.BasePositionScreen
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun SplitScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
    onAddSplit: () -> Unit,
    onDeleteSplit: (DbSplit) -> Unit,
    onUpdateSplit: (DbSplit) -> Unit,
) {
  val isLoading = state.isLoading
  val splits = state.stockSplits

  val positionError = state.positionsError
  val holdingError = state.holdingError

  val isAddVisible =
      remember(isLoading, positionError, holdingError) {
        !isLoading && positionError == null && holdingError == null
      }

  BasePositionScreen(
      modifier = modifier,
      label = "Add Stock Split",
      isAddVisible = isAddVisible,
      items = splits,
      isLoading = isLoading,
      onRefresh = onRefresh,
      onAddClicked = onAddSplit,
      itemKey = { it.id().raw },
      renderListItem = { split ->
        SplitItem(
            modifier =
                Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = { onUpdateSplit(split) },
                        onLongClick = { onDeleteSplit(split) },
                    ),
            split = split,
        )
      },
  )
}

@Preview
@Composable
private fun PreviewSplitScreen() {
  val symbol = "MSFT".asSymbol()
  Surface {
    SplitScreen(
        modifier = Modifier.fillMaxSize(),
        state =
            MutablePortfolioDigViewState(
                symbol = symbol,
                lookupSymbol = symbol,
                equityType = EquityType.STOCK,
                tradeSide = TradeSide.BUY,
            ),
        onAddSplit = {},
        onRefresh = {},
        onDeleteSplit = {},
        onUpdateSplit = {},
    )
  }
}
