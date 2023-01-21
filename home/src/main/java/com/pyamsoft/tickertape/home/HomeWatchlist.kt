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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.tickertape.home.item.HomeWatchlistItem
import com.pyamsoft.tickertape.quote.Ticker
import kotlinx.coroutines.CoroutineScope

@Composable
internal fun HomeWatchlist(
    modifier: Modifier = Modifier,
    state: HomeWatchListViewState,
    onClicked: (Ticker) -> Unit,
    onRefresh: CoroutineScope.() -> Unit,
) {
  val loadingState by state.isLoadingWatchlist.collectAsState()
  val error by state.watchlistError.collectAsState()
  val tickers = state.watchlist.collectAsStateList()

  val isLoading = remember(loadingState) { loadingState == HomeBaseViewState.LoadingState.LOADING }

  val isEmptyTickers = remember(tickers) { tickers.isEmpty() }
  val isVisible =
      remember(
          isEmptyTickers,
          isLoading,
      ) {
        !isEmptyTickers || isLoading
      }

  FirstRenderEffect { onRefresh() }

  Crossfade(
      modifier = modifier,
      targetState = error,
  ) { err ->
    if (err == null) {
      Column {
        AnimatedVisibility(
            visible = isVisible,
        ) {
          Text(
              modifier =
                  Modifier.padding(start = MaterialTheme.keylines.content)
                      .padding(bottom = MaterialTheme.keylines.baseline),
              text = "My Watchlist",
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W400,
                  ),
          )
        }

        Box {
          TickerList(
              // Don't use matchParentSize here
              modifier = Modifier.fillMaxWidth(),
              tickers = tickers,
              onClick = onClicked,
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
private fun TickerList(
    modifier: Modifier = Modifier,
    tickers: List<Ticker>,
    onClick: (Ticker) -> Unit,
) {
  LazyRow(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.baseline),
  ) {
    itemsIndexed(
        items = tickers,
        key = { index, item -> "${item.symbol.raw}-${index}" },
    ) { index, item ->
      // We can assume here the chart is not null
      HomeWatchlistItem(
          modifier =
              Modifier.width(HomeScreenDefaults.rememberItemWidth()).run {
                when (index) {
                  0 -> padding(start = MaterialTheme.keylines.content)
                  tickers.lastIndex -> padding(end = MaterialTheme.keylines.content)
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
private fun PreviewWatchlist() {
  Surface {
    HomeWatchlist(
        state = MutableHomeViewState(),
        onClicked = {},
        onRefresh = {},
    )
  }
}
