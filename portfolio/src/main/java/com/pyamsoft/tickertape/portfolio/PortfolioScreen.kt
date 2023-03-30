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

package com.pyamsoft.tickertape.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.ImageLoader
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.tickertape.portfolio.item.PorfolioSummaryItem
import com.pyamsoft.tickertape.portfolio.item.PortfolioItem
import com.pyamsoft.tickertape.quote.base.BaseListScreen
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.ui.PolinaGolubevaScreen
import com.pyamsoft.tickertape.ui.test.createNewTestImageLoader
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

@Composable
@JvmOverloads
fun PortfolioScreen(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
    imageLoader: ImageLoader,
    onRefresh: () -> Unit,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTabUpdated: (EquityType) -> Unit,
    onRegenerateList: CoroutineScope.() -> Unit,
    onHoldingDeleteFinalized: () -> Unit,
    onHoldingRestored: () -> Unit,
) {
  val loadingState by state.loadingState.collectAsState()
  val pageError by state.error.collectAsState()
  val search by state.query.collectAsState()
  val tab by state.section.collectAsState()
  val list = state.stocks.collectAsStateList()

  val isLoading = remember(loadingState) { loadingState == PortfolioViewState.LoadingState.LOADING }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomCenter,
  ) {
    BaseListScreen(
        modifier = Modifier.fillMaxSize(),
        imageLoader = imageLoader,
        isLoading = isLoading,
        pageError = pageError,
        list = list,
        search = search,
        tab = tab,
        onRefresh = onRefresh,
        onSearchChanged = onSearchChanged,
        onTabUpdated = onTabUpdated,
        onRegenerateList = onRegenerateList,
        itemKey = { _, stock -> stock.holding.symbol.raw },
        renderHeader = {
          PortfolioSummary(
              modifier = Modifier.fillMaxWidth(),
              state = state,
          )
        },
        renderListItem = { stock ->
          PortfolioItem(
              modifier =
                  Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.baseline),
              stock = stock,
              onSelect = onSelect,
              onDelete = onDelete,
          )
        },
        renderEmptyState = {
          EmptyState(
              modifier =
                  Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.baseline),
              imageLoader = imageLoader,
          )
        },
    )

    PortfolioSnackbar(
        state = state,
        onSnackbarDismissed = onHoldingDeleteFinalized,
        onSnackbarAction = onHoldingRestored,
    )
  }
}

@Composable
private fun PortfolioSnackbar(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
) {
  val deleteUndoState = remember { SnackbarHostState() }
  val undoable by state.recentlyDeleteHolding.collectAsState()

  SnackbarHost(
      modifier = modifier,
      hostState = deleteUndoState,
  )

  undoable?.also { u ->
    LaunchedEffect(u) {
      val snackbarResult =
          deleteUndoState.showSnackbar(
              message = "${u.symbol.raw} removed",
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

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
) {
  PolinaGolubevaScreen(
      modifier = modifier,
      imageLoader = imageLoader,
      image = R.drawable.portfolio_empty,
      bottomContent = {
        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.keylines.content),
            text = "Nothing in your portfolio, add a position!",
            style = MaterialTheme.typography.h6,
        )
      },
  )
}

@Composable
private fun PortfolioSummary(
    modifier: Modifier = Modifier,
    state: PortfolioViewState,
) {
  val portfolio by state.portfolio.collectAsState()
  val section by state.section.collectAsState()

  val data =
      remember(section, portfolio) {
        when (section) {
          EquityType.STOCK -> portfolio?.stocks
          EquityType.OPTION -> {
            Timber.d("Don't show a summary for options")
            null
          }
          EquityType.CRYPTOCURRENCY -> portfolio?.crypto
        }
      }

  AnimatedVisibility(
      visible = data != null,
  ) {
    data?.also { d ->
      PorfolioSummaryItem(
          modifier =
              modifier
                  .statusBarsPadding()
                  .padding(horizontal = MaterialTheme.keylines.content)
                  .padding(top = MaterialTheme.keylines.baseline),
          data = d,
      )
    }
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
      onRegenerateList = {},
      onHoldingDeleteFinalized = {},
      onHoldingRestored = {},
  )
}
