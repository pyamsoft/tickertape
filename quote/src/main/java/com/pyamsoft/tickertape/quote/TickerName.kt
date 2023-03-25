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

package com.pyamsoft.tickertape.quote

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
fun TickerName(
    modifier: Modifier = Modifier,
    symbol: StockSymbol,
    ticker: Ticker?,
    size: TickerSize,
) {
  val quote = ticker?.quote
  val typography = MaterialTheme.typography
  val contentColor = LocalContentColor.current

  val highAlpha = ContentAlpha.high
  val mediumAlpha = ContentAlpha.medium

  val sizes =
      remember(
          size,
          typography,
          contentColor,
          highAlpha,
          mediumAlpha,
      ) {
        when (size) {
          TickerSize.CHART ->
              TickerSizes.chart(
                  typography,
                  contentColor,
                  highAlpha,
                  mediumAlpha,
              )
          TickerSize.QUOTE ->
              TickerSizes.quote(
                  typography,
                  contentColor,
                  highAlpha,
                  mediumAlpha,
              )
          else -> throw IllegalStateException("Can't use TickerName with size: $size")
        }
      }

  Column(
      modifier = modifier,
  ) {
    Text(
        text = symbol.raw,
        style = sizes.title,
    )

    if (quote != null) {
      if (quote.company.isValid) {
        Text(
            text = quote.company.company,
            style = sizes.description,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewTickerName() {
  val clock = TestClock
  val symbol = TestSymbol

  Surface {
    TickerName(
        symbol = symbol,
        ticker =
            Ticker(
                symbol = symbol,
                quote = newTestQuote(symbol),
                chart = newTestChart(symbol, clock),
            ),
        size = TickerSize.QUOTE,
    )
  }
}
