/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.home.item.HomePortfolioSummaryItem
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun HomePortfolio(
    modifier: Modifier = Modifier,
    state: HomePortfolioViewState,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingPortfolio.collectAsStateWithLifecycle()
  val portfolio by state.portfolio.collectAsStateWithLifecycle()
  val portfolioError by state.portfolioError.collectAsStateWithLifecycle()

  val isEmptyPortfolio =
      remember(portfolio) { portfolio.let { it == null || it.stocks.positions.isEmpty() } }
  val isLoading = remember(loadingState) { loadingState == HomeBaseViewState.LoadingState.LOADING }

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
      label = "Portfolio Summary",
      targetState = portfolioError,
  ) { err ->
    if (err == null) {
      Column {
        AnimatedVisibility(
            visible = isVisible,
        ) {
          Text(
              modifier =
                  Modifier.padding(
                      start = MaterialTheme.keylines.content,
                      bottom = MaterialTheme.keylines.baseline,
                  ),
              text = "My Portfolio Summary",
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W400,
                  ),
          )
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(HomeScreenDefaults.PORTFOLIO_HEIGHT),
        ) {
          portfolio?.also { p ->
            HomePortfolioSummaryItem(
                modifier =
                    Modifier.matchParentSize().padding(horizontal = MaterialTheme.keylines.content),
                portfolio = p,
            )
          }

          Loading(
              modifier = Modifier.matchParentSize(),
              isLoading = isLoading,
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
      modifier = modifier,
      visible = isLoading,
      enter = fadeIn(),
      exit = fadeOut(),
  ) {
    Box(
        modifier = modifier.padding(MaterialTheme.keylines.content),
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
        state = MutableHomeViewState(),
        onRefresh = {},
    )
  }
}
