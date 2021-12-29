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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.tickertape.home.item.HomePortfolioSummaryItem
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalAnimationApi::class)
internal fun HomePortfolio(
    modifier: Modifier = Modifier,
    state: HomePortfolioViewState,
) {
  val isLoading = state.isLoadingPortfolio
  val portfolio = state.portfolio
  val error = state.portfolioError

  val count = remember(portfolio) { portfolio.list.size }
  val isVisible = remember(count) { count > 0 }
  val isListVisible = remember(isVisible, isLoading) { isVisible || isLoading }

  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      Column {
        AnimatedVisibility(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            visible = isVisible,
        ) {
          Text(
              text = "My Portfolio Summary",
              style =
                  MaterialTheme.typography.h6.copy(
                      fontWeight = FontWeight.Bold,
                  ),
          )
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth().height(HomeScreenDefaults.PORTFOLIO_HEIGHT_DP.dp),
            visible = isListVisible,
        ) {
          Box {
            HomePortfolioSummaryItem(
                modifier = Modifier.matchParentSize().padding(horizontal = 16.dp),
                portfolio = portfolio,
            )

            Loading(
                isLoading = isLoading,
                modifier = Modifier.matchParentSize(),
            )
          }
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
@OptIn(ExperimentalAnimationApi::class)
private fun Loading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
      visible = isLoading,
      modifier = modifier,
  ) {
    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator() }
  }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier,
    error: Throwable,
) {
  Column(
      modifier = modifier.padding(16.dp),
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
        modifier = Modifier.padding(top = 16.dp),
        textAlign = TextAlign.Center,
        text = "Please try again later.",
        style = MaterialTheme.typography.body2,
    )
  }
}

@Preview
@Composable
private fun PreviewWatchlist() {
  val symbol = "MSFT".asSymbol()
  Surface {
    HomeWatchlist(
        state =
            object : HomeWatchListViewState {
              override val watchlist: List<Ticker> =
                  listOf(
                      Ticker(
                          symbol = symbol,
                          quote = newTestQuote(symbol),
                          chart = newTestChart(symbol),
                      ),
                  )
              override val watchlistError: Throwable? = null
              override val isLoadingWatchlist: Boolean = false
            },
        onClicked = {},
    )
  }
}
