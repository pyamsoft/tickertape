package com.pyamsoft.tickertape.portfolio.dig.splits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.SplitsPortfolioDigViewState
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.base.BaseDigListScreen
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun SplitScreen(
    modifier: Modifier = Modifier,
    state: SplitsPortfolioDigViewState,
    onRefresh: () -> Unit,
    onAddSplit: (DbHolding) -> Unit,
    onUpdateSplit: (DbSplit, DbHolding) -> Unit,
    onDeleteSplit: (DbSplit) -> Unit,
) {
  val loadingState by state.loadingState.collectAsState()
  val splitError by state.stockSplitError.collectAsState()
  val holding by state.holding.collectAsState()
  val holdingError by state.holdingError.collectAsState()
  val splits = state.stockSplits.collectAsStateList()

  val isAddVisible =
      remember(loadingState, splitError, holdingError) {
        loadingState == BaseDigViewState.LoadingState.DONE &&
            splitError == null &&
            holdingError == null
      }

  val isLoading = remember(loadingState) { loadingState == BaseDigViewState.LoadingState.LOADING }

  holding.also { h ->
    if (h == null) {
      CircularProgressIndicator(
          modifier = Modifier.size(64.dp),
      )
    } else {
      BaseDigListScreen(
          modifier = modifier,
          label = "Add Stock Split",
          isAddVisible = isAddVisible,
          items = splits,
          isLoading = isLoading,
          onRefresh = onRefresh,
          onAddClicked = { onAddSplit(h) },
          itemKey = { it.id.raw },
      ) { split ->
        SplitItem(
            modifier =
                Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = { onUpdateSplit(split, h) },
                        onLongClick = { onDeleteSplit(split) },
                    ),
            split = split,
        )
      }
    }
  }
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
                params =
                    PortfolioDigParams(
                        symbol = symbol,
                        lookupSymbol = null,
                    ),
            ),
        onAddSplit = {},
        onRefresh = {},
        onDeleteSplit = {},
        onUpdateSplit = { _, _ -> },
    )
  }
}
