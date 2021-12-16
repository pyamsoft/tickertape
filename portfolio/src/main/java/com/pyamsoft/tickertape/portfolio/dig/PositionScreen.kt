package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
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
import com.pyamsoft.tickertape.stocks.api.asSymbol

private const val POSITIONS_LIST_MAX_HEIGHT = 360

@Composable
@JvmOverloads
internal fun PositionScreen(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
    onAddPosition: () -> Unit,
) {
  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    PositionsList(
        modifier = Modifier.fillMaxWidth().heightIn(max = POSITIONS_LIST_MAX_HEIGHT.dp),
        state = state,
        onRefresh = onRefresh,
    )

    PositionsAdd(
        state = state,
        onAddPosition = onAddPosition,
    )
  }
}

@Composable
private fun PositionsList(
    modifier: Modifier = Modifier,
    state: PortfolioDigViewState,
    onRefresh: () -> Unit,
) {
  val isLoading = state.isLoading
  SwipeRefresh(
      modifier = modifier,
      state = rememberSwipeRefreshState(isRefreshing = isLoading),
      onRefresh = onRefresh,
  ) {
    LazyColumn {
      item { Box { Text(text = "Placeholder 1!") } }

      item { Box { Text(text = "Placeholder 2!") } }

      item { Box { Text(text = "Placeholder 3!") } }
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
      modifier = modifier.padding(16.dp),
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
  PositionScreen(
      state =
          MutablePortfolioDigViewState(
              symbol = "MSFT".asSymbol(),
          ),
      onAddPosition = {},
      onRefresh = {},
  )
}
