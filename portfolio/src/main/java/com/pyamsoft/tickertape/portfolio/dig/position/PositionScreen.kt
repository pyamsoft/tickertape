package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PositionsPortfolioDigViewState
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.base.BaseDigListScreen
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun PositionScreen(
    modifier: Modifier = Modifier,
    state: PositionsPortfolioDigViewState,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
    onUpdatePosition: (DbPosition) -> Unit,
) {
  val loadingState by state.loadingState.collectAsState()
  val positionError by state.positionsError.collectAsState()
  val holdingError by state.holdingError.collectAsState()
  val positions = state.positions.collectAsStateList()

  val isAddVisible =
      remember(loadingState, positionError, holdingError) {
        loadingState == BaseDigViewState.LoadingState.DONE &&
            positionError == null &&
            holdingError == null
      }

  val isLoading = remember(loadingState) { loadingState == BaseDigViewState.LoadingState.LOADING }

  BaseDigListScreen(
      modifier = modifier,
      label = "Add Position",
      isAddVisible = isAddVisible,
      items = positions,
      isLoading = isLoading,
      onRefresh = onRefresh,
      onAddClicked = onAddPosition,
      itemKey = { it.id.raw },
  ) { position ->
    PositionItem(
        modifier =
            Modifier.fillMaxWidth()
                .combinedClickable(
                    onClick = { onUpdatePosition(position) },
                    onLongClick = { onDeletePosition(position) },
                ),
        position = position,
    )
  }
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
            ),
        onAddPosition = {},
        onRefresh = {},
        onDeletePosition = {},
        onUpdatePosition = {},
    )
  }
}
