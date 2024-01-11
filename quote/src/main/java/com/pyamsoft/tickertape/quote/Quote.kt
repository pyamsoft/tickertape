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

package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.BorderCard

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Quote(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    ticker: Ticker?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable QuoteScope.() -> Unit = {},
) {
  BorderCard(
      modifier = modifier,
  ) {
    Column(
        modifier =
            Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .fillMaxWidth()
                .padding(MaterialTheme.keylines.content),
    ) {
      TickerName(
          modifier = Modifier.fillMaxWidth(),
          symbol = symbol,
          ticker = ticker,
          size = TickerSize.QUOTE,
      )

      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
      ) {
        Column(
            modifier = Modifier.weight(1F).padding(top = MaterialTheme.keylines.content),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          DefaultQuoteScopeInstance.content()
        }

        Column(
            modifier =
                Modifier.padding(start = MaterialTheme.keylines.baseline)
                    .padding(top = MaterialTheme.keylines.baseline),
            horizontalAlignment = Alignment.End,
        ) {
          TickerPrice(
              ticker = ticker,
              size = TickerSize.QUOTE,
          )
        }
      }
    }
  }
}

@Preview
@Composable
private fun PreviewQuote() {
  val symbol = TestSymbol

  Quote(
      modifier = Modifier.padding(16.dp),
      symbol = symbol,
      ticker =
          Ticker(
              symbol = symbol,
              quote = newTestQuote(symbol),
              chart = null,
          ),
      onClick = {},
      onLongClick = {},
  )
}
