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

package com.pyamsoft.tickertape.portfolio.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroSize
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.test.newTestHolding
import com.pyamsoft.tickertape.quote.Quote
import com.pyamsoft.tickertape.quote.QuoteScope
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.asGainLoss
import com.pyamsoft.tickertape.ui.LongTermPurchaseDateTag
import com.pyamsoft.tickertape.ui.ShortTermPurchaseDateTag
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
@JvmOverloads
internal fun PortfolioItem(
    modifier: Modifier = Modifier,
    stock: PortfolioStock,
    onSelect: (PortfolioStock) -> Unit,
    onDelete: (PortfolioStock) -> Unit,
) {
  Quote(
      modifier = modifier.fillMaxWidth(),
      symbol = stock.holding.symbol,
      ticker = stock.ticker,
      onClick = { onSelect(stock) },
      onLongClick = { onDelete(stock) },
  ) {
    Column {
      val shareCount = stock.totalShares
      if (shareCount.isValid && !shareCount.isZero) {
        PositionData(
            stock = stock,
        )
      } else {
        QuoteData(
            ticker = stock.ticker,
        )
      }
    }
  }
}

@Composable
private fun QuoteScope.QuoteData(
    ticker: Ticker?,
) {
  val quote = ticker?.quote

  if (quote != null) {
    Column {
      Row(
          verticalAlignment = Alignment.CenterVertically,
      ) {
        quote.dayLow?.also { low ->
          Info(
              modifier = Modifier.padding(end = MaterialTheme.keylines.content),
              name = "Low",
              value = low.display,
          )
        }

        quote.dayHigh?.also { high ->
          Info(
              name = "High",
              value = high.display,
          )
        }
      }

      quote.dayVolume?.also { volume ->
        Info(
            name = "Volume",
            value = volume.display,
        )
      }
    }
  }
}

@Composable
private fun QuoteScope.PositionData(
    stock: PortfolioStock,
) {
  val totalDirection = stock.totalDirection
  val totalChangeTitle = remember(totalDirection) { totalDirection.asGainLoss() }

  Row(
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Info(
        modifier = Modifier.padding(end = MaterialTheme.keylines.content),
        name = if (stock.isOption) "Contracts" else "Shares",
        value = stock.totalShares.display,
    )

    // These are only valid if we have current day quotes
    if (stock.ticker != null) {
      Info(
          name = "$totalChangeTitle Percent",
          value = stock.totalGainLossPercent,
      )
    } else {
      Info(
          name = "Value",
          value = stock.current.display,
      )
    }
  }

  if (stock.shortTermPositions > 0 || stock.longTermPositions > 0) {
    Info(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.typography),
        name = "Positions",
        // No value here
        value = "",
    )
  }

  if (stock.shortTermPositions > 0) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      ShortTermPurchaseDateTag()
      Info(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          value = "${stock.shortTermPositions}",
      )
    }
  }

  if (stock.longTermPositions > 0) {
    Row(
        modifier =
            Modifier.padding(
                top =
                    if (stock.shortTermPositions > 0) MaterialTheme.keylines.baseline else ZeroSize,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      LongTermPurchaseDateTag()
      Info(
          modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
          value = "${stock.longTermPositions}",
      )
    }
  }
}

@Preview
@Composable
private fun PreviewPortfolioItem() {
  val symbol = TestSymbol
  val clock = TestClock

  PortfolioItem(
      stock =
          PortfolioStock(
              holding = newTestHolding(symbol),
              positions = emptyList(),
              ticker =
                  Ticker(
                      symbol = symbol,
                      quote = newTestQuote(symbol),
                      chart = null,
                  ),
              splits = emptyList(),
              clock = clock,
          ),
      onSelect = {},
      onDelete = {},
  )
}
