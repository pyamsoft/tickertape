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

package com.pyamsoft.tickertape.quote.dig.recommend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.TickerName
import com.pyamsoft.tickertape.quote.TickerPrice
import com.pyamsoft.tickertape.quote.TickerSize
import com.pyamsoft.tickertape.quote.chart.Chart
import com.pyamsoft.tickertape.quote.dig.DigDefaults
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.RecommendationDigViewState
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.ui.BorderCard
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
fun DigRecommendations(
    modifier: Modifier = Modifier,
    state: RecommendationDigViewState,
    onRefresh: () -> Unit,
    onRecClick: (Ticker) -> Unit,
) {
  val error by state.recommendationError.collectAsStateWithLifecycle()
  val recommendations by state.recommendations.collectAsStateWithLifecycle()

  LazyColumn(
      modifier = modifier.fillMaxSize().padding(MaterialTheme.keylines.content),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.content),
  ) {
    if (error == null) {
      items(
          items = recommendations,
          key = { it.ticker.symbol.raw },
      ) { rec ->
        RecommendationItem(
            modifier = Modifier.fillMaxWidth(),
            recommendation = rec,
            onClick = onRecClick,
        )
      }
    } else {
      item {
        val errorMessage =
            remember(error) { error.requireNotNull().message ?: "An unexpected error occurred" }

        Text(
            text = errorMessage,
            style =
                MaterialTheme.typography.h6.copy(
                    color = MaterialTheme.colors.error,
                ),
        )
      }
    }
  }
}

@Composable
private fun RecommendationItem(
    modifier: Modifier = Modifier,
    recommendation: StockRec,
    onClick: (Ticker) -> Unit,
) {
  val ticker = recommendation.ticker
  val chart = recommendation.chart

  val isRecommendationSpecialSession =
      remember(ticker) {
        val quote = ticker.quote
        if (quote == null) {
          return@remember false
        } else {
          return@remember quote.afterHours != null || quote.preMarket != null
        }
      }

  BorderCard(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.clickable { onClick(ticker) }.padding(MaterialTheme.keylines.baseline),
    ) {
      TickerName(
          modifier = Modifier.fillMaxWidth(),
          symbol = ticker.symbol,
          ticker = ticker,
          size = TickerSize.QUOTE,
      )
      Chart(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(top = MaterialTheme.keylines.content)
                  .padding(bottom = MaterialTheme.keylines.baseline)
                  .height(DigDefaults.rememberChartHeight()),
          painter = chart,
      )
      Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.BottomEnd,
      ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
        ) {
          TickerPrice(
              ticker = ticker,
              size = TickerSize.RECOMMEND_QUOTE,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewDigRecommendations() {
  val symbol = TestSymbol
  val clock = TestClock

  DigRecommendations(
      state =
          object :
              MutableDigViewState(
                  symbol = symbol,
                  clock = clock,
              ) {},
      onRefresh = {},
      onRecClick = {},
  )
}
