/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.MutablePortfolioDigViewState
import com.pyamsoft.tickertape.portfolio.dig.PositionsPortfolioDigViewState
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.base.BaseDigListScreen
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.test.TestClock
import java.time.Clock

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun PositionScreen(
    modifier: Modifier = Modifier,
    state: PositionsPortfolioDigViewState,
    clock: Clock,
    onRefresh: () -> Unit,
    onAddPosition: (DbHolding) -> Unit,
    onUpdatePosition: (DbPosition, DbHolding) -> Unit,
    onDeletePosition: (DbPosition) -> Unit,
    onPositionRestored: () -> Unit,
    onPositionDeleteFinalized: () -> Unit,
) {

  val loadingState by state.loadingState.collectAsState()
  val positionError by state.positionsError.collectAsState()
  val holding by state.holding.collectAsState()
  val holdingError by state.holdingError.collectAsState()
  val positions = state.positions.collectAsStateList()

  val isAddVisible =
      remember(loadingState, positionError, holdingError) {
        loadingState == BaseDigViewState.LoadingState.DONE &&
            positionError == null &&
            holdingError == null
      }

  val isLoading = remember(loadingState) { loadingState == BaseDigViewState.LoadingState.LOADING }

  holding.also { h ->
    if (h == null) {
      CircularProgressIndicator(
          modifier = Modifier.size(64.dp),
      )
    } else {
      Box(
          modifier = modifier,
          contentAlignment = Alignment.BottomCenter,
      ) {
        BaseDigListScreen(
            modifier = Modifier.matchParentSize(),
            label = "Add Position",
            isAddVisible = isAddVisible,
            items = positions,
            isLoading = isLoading,
            onRefresh = onRefresh,
            onAddClicked = { onAddPosition(h) },
            itemKey = { it.id.raw },
        ) { position ->
          PositionItem(
              modifier =
                  Modifier.fillMaxWidth()
                      .combinedClickable(
                          onClick = { onUpdatePosition(position, h) },
                          onLongClick = { onDeletePosition(position) },
                      ),
              position = position,
              clock = clock,
          )
        }

        PositionSnackbar(
            state = state,
            symbol = h.symbol,
            onSnackbarDismissed = onPositionDeleteFinalized,
            onSnackbarAction = onPositionRestored,
        )
      }
    }
  }
}

@Composable
private fun PositionSnackbar(
    modifier: Modifier = Modifier,
    state: PositionsPortfolioDigViewState,
    symbol: StockSymbol,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
) {
  val deleteUndoState = remember { SnackbarHostState() }
  val undoable by state.recentlyDeletePosition.collectAsState()

  SnackbarHost(
      modifier = modifier,
      hostState = deleteUndoState,
  )

  undoable?.also { u ->
    LaunchedEffect(u) {
      val snackbarResult =
          deleteUndoState.showSnackbar(
              message = "${symbol.raw} position removed",
              duration = SnackbarDuration.Short,
              actionLabel = "Undo",
          )

      when (snackbarResult) {
        SnackbarResult.Dismissed -> {
          onSnackbarDismissed()
        }
        SnackbarResult.ActionPerformed -> {
          onSnackbarAction()
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewPositionScreen() {
  val symbol = TestSymbol
  val clock = TestClock

  PositionScreen(
      modifier = Modifier.fillMaxSize(),
      clock = clock,
      state =
          MutablePortfolioDigViewState(
              params =
                  PortfolioDigParams(
                      symbol = symbol,
                      lookupSymbol = null,
                  ),
              clock = clock,
          ),
      onAddPosition = {},
      onRefresh = {},
      onDeletePosition = {},
      onUpdatePosition = { _, _ -> },
      onPositionDeleteFinalized = {},
      onPositionRestored = {},
  )
}
