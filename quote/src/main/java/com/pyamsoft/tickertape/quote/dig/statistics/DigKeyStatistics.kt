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

package com.pyamsoft.tickertape.quote.dig.statistics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.StatisticsDigViewState
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
fun DigKeyStatistics(
    modifier: Modifier = Modifier,
    state: StatisticsDigViewState,
    onRefresh: () -> Unit,
) {
  val error by state.statisticsError.collectAsStateWithLifecycle()
  val statistics by state.statistics.collectAsStateWithLifecycle()

  LazyColumn(
      modifier = modifier.fillMaxSize().padding(MaterialTheme.keylines.content),
  ) {
    if (error == null) {
      statistics?.let { stats ->
        renderFinancialHighlights(
            statistics = stats,
        )
        renderTradingInformation(
            statistics = stats,
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

@Preview
@Composable
private fun PreviewDigKeyStatistics() {
  val symbol = TestSymbol
  val clock = TestClock

  DigKeyStatistics(
      state =
          object :
              MutableDigViewState(
                  symbol = symbol,
                  clock = clock,
              ) {},
      onRefresh = {},
  )
}
