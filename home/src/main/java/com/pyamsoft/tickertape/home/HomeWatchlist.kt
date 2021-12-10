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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.pyamsoft.tickertape.home.item.HomeWatchlistItem
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asSymbol

@Composable
@OptIn(ExperimentalAnimationApi::class)
internal fun HomeWatchlist(
    modifier: Modifier = Modifier,
    state: HomeWatchListViewState,
    onClicked: (Ticker) -> Unit,
) {
  val isLoading = state.isLoadingWatchlist
  val tickers = state.watchlist
  val error = state.watchlistError

  val count = remember(tickers) { tickers.size }

  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      Column {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            text = "My Watchlist${if (count > 0) " Top $count" else ""}",
            style =
                MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold,
                ),
        )

        val columnScope = this
        Box(
            modifier = Modifier.fillMaxWidth().height(HomeScreenDefaults.WATCHLIST_HEIGHT_DP.dp),
        ) {
          TickerList(
              modifier = Modifier.matchParentSize(),
              tickers = tickers,
              onClick = onClicked,
          )

          columnScope.AnimatedVisibility(
              visible = isLoading,
              modifier = Modifier.matchParentSize(),
          ) {
            Loading(
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
private fun Loading(
    modifier: Modifier = Modifier,
) {
  Box(
      modifier = modifier.padding(16.dp),
      contentAlignment = Alignment.Center,
  ) { CircularProgressIndicator() }
}

@Composable
private fun TickerList(
    modifier: Modifier = Modifier,
    tickers: List<Ticker>,
    onClick: (Ticker) -> Unit,
) {
  LazyRow(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    itemsIndexed(
        items = tickers,
        key = { _, item -> item.symbol.symbol() },
    ) { index, item ->
      // We can assume here the chart is not null
      HomeWatchlistItem(
          modifier =
              Modifier.fillMaxHeight().width(HomeScreenDefaults.ITEM_WIDTH_DP.dp).run {
                when (index) {
                  0 -> padding(start = 16.dp)
                  tickers.lastIndex -> padding(end = 16.dp)
                  else -> this
                }
              },
          ticker = item,
          onClick = onClick,
      )
    }
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
