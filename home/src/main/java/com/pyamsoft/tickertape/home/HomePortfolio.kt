/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.tickertape.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.home.item.HomePortfolioSummaryItem
import com.pyamsoft.tickertape.portfolio.PortfolioStockList
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun HomePortfolio(
    modifier: Modifier = Modifier,
    state: HomePortfolioViewState,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val isLoading = state.isLoadingPortfolio
  val portfolio = state.portfolio

  val isEmptyPortfolio = remember(portfolio) { portfolio.list.isEmpty() }
  val isVisible =
      remember(
          isEmptyPortfolio,
          isLoading,
      ) {
        !isEmptyPortfolio || isLoading
      }

  // As long as we are blank
  LaunchedEffect(isEmptyPortfolio) {
    val scope = this

    // Load even if not currently visible
    scope.onRefresh()
  }

  Crossfade(
      modifier = modifier,
      targetState = state.portfolioError,
  ) { err ->
    if (err == null) {
      Column {
        AnimatedVisibility(
            modifier =
                Modifier.padding(
                    start = MaterialTheme.keylines.content,
                    bottom = MaterialTheme.keylines.baseline,
                ),
            visible = isVisible,
        ) {
          Text(
              text = "My Portfolio Summary",
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W400,
                  ),
          )
        }

        Box {
          HomePortfolioSummaryItem(
              // Don't use matchParentSize here
              modifier =
                  Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
              portfolio = portfolio,
          )

          Loading(
              isLoading = isLoading,
              modifier = Modifier.matchParentSize(),
          )
        }
      }
    } else {
      Error(
          modifier = Modifier.fillMaxWidth(),
          error = err,
      )
    }
  }
}

@Composable
private fun Loading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
      visible = isLoading,
      modifier = modifier,
  ) {
    Box(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator()
    }
  }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier,
    error: Throwable,
) {
  Column(
      modifier = modifier.padding(MaterialTheme.keylines.content),
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
        modifier = Modifier.padding(top = MaterialTheme.keylines.content),
        textAlign = TextAlign.Center,
        text = "Please try again later.",
        style = MaterialTheme.typography.body2,
    )
  }
}

@Preview
@Composable
private fun PreviewPortfolio() {
  Surface {
    HomePortfolio(
        state =
            object : HomePortfolioViewState {
              override val portfolio: PortfolioStockList = PortfolioStockList.empty()
              override val portfolioError: Throwable? = null
              override val isLoadingPortfolio: Boolean = false
            },
        onRefresh = {},
    )
  }
}
