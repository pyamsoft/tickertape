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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.quote.test.TestSymbol
import com.pyamsoft.tickertape.quote.test.newTestChart
import com.pyamsoft.tickertape.quote.test.newTestQuote
import com.pyamsoft.tickertape.stocks.api.MarketState
import com.pyamsoft.tickertape.ui.test.TestClock

@Composable
fun TickerPrice(
    modifier: Modifier = Modifier,
    ticker: Ticker?,
    size: TickerSize,
) {
  val quote = ticker?.quote
  if (quote != null) {
    val typography = MaterialTheme.typography
    val contentColor = LocalContentColor.current
    val highAlpha = ContentAlpha.high
    val mediumAlpha = ContentAlpha.medium
    val disabledAlpha = ContentAlpha.disabled

    val sizes =
        remember(
            size,
            typography,
            contentColor,
            highAlpha,
            mediumAlpha,
            disabledAlpha,
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
                TickerSizes.price(
                    typography,
                    contentColor,
                    highAlpha,
                    mediumAlpha,
                )
            TickerSize.RECOMMEND_QUOTE ->
                TickerSizes.recPrice(
                    typography,
                    contentColor,
                    highAlpha,
                    mediumAlpha,
                )
          }
        }

    // If we are a special quote, display the regular market info
    val session = quote.currentSession

    val direction = session.direction
    val directionSign = session.direction.sign
    val composeColor =
        remember(direction) {
          return@remember if (direction.isZero || !direction.isValid) {
            Color.Unspecified
          } else {
            Color(direction.color)
          }
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
      Text(
          text =
              when (session.state) {
                MarketState.REGULAR -> "Normal Market"
                MarketState.POST -> "After Hours"
                MarketState.PRE -> "Pre-Market"
              },
          style = sizes.label,
      )
      PriceSection(
          value = session.price.display,
          valueStyle = sizes.title.copy(color = composeColor),
          changeAmount = "${directionSign}${session.amount.display}",
          changePercent = "(${directionSign}${session.percent.display})",
          changeStyle = sizes.description.copy(color = composeColor),
      )
    }
  }
}

@Composable
@JvmOverloads
fun PriceSection(
    modifier: Modifier = Modifier,
    value: String,
    valueStyle: TextStyle,
    changeAmount: String,
    changePercent: String,
    changeStyle: TextStyle,
) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.End,
  ) {
    if (value.isNotBlank()) {
      Text(
          text = value,
          style = valueStyle,
      )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      if (changeAmount.isNotBlank()) {
        Text(
            text = changeAmount,
            style = changeStyle,
        )
      }
      if (changePercent.isNotBlank()) {
        Text(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            text = changePercent,
            style = changeStyle,
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewTickerPrice() {
  val clock = TestClock
  val symbol = TestSymbol

  Surface {
    TickerPrice(
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
