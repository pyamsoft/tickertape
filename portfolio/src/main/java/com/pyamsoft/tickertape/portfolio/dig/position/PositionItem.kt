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

package com.pyamsoft.tickertape.portfolio.dig.position

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.priceWithSplits
import com.pyamsoft.tickertape.db.position.shareCountWithSplits
import com.pyamsoft.tickertape.portfolio.test.newTestPosition
import com.pyamsoft.tickertape.quote.DefaultQuoteScopeInstance
import com.pyamsoft.tickertape.quote.PriceSection
import com.pyamsoft.tickertape.quote.QuoteScope
import com.pyamsoft.tickertape.quote.TickerSizes
import com.pyamsoft.tickertape.stocks.api.DATE_FORMATTER
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asShares
import com.pyamsoft.tickertape.ui.BorderCard
import com.pyamsoft.tickertape.ui.PurchaseDateTag
import com.pyamsoft.tickertape.ui.test.TestClock
import java.time.Clock
import java.time.LocalDate

@Composable
@JvmOverloads
internal fun PositionItem(
    modifier: Modifier = Modifier,
    position: PositionStock,
    clock: Clock,
) {
  BorderCard(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
    ) {
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom,
      ) {
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          DefaultQuoteScopeInstance.apply {
            PurchaseDate(
                modifier = Modifier.padding(bottom = MaterialTheme.keylines.content),
                position = position,
                clock = clock,
            )
            NumberOfShares(
                position = position,
            )
            CostBasis(
                position = position,
            )
          }
        }

        Column(
            modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
            horizontalAlignment = Alignment.End,
        ) {
          CurrentPosition(
              position = position,
          )
        }
      }
    }
  }
}

@Composable
private fun CurrentPosition(
    modifier: Modifier = Modifier,
    position: PositionStock,
) {
  val contentColor = LocalContentColor.current
  val typography = MaterialTheme.typography
  val highAlpha = ContentAlpha.high
  val mediumAlpha = ContentAlpha.medium

  val sizes =
      remember(
          typography,
          contentColor,
          highAlpha,
          mediumAlpha,
      ) {
        TickerSizes.price(
            typography,
            contentColor,
            highAlpha,
            mediumAlpha,
        )
      }

  val composeColor =
      remember(position) {
        val direction = position.gainLossPercent.asDirection()
        return@remember if (direction.isZero || !direction.isValid) {
          Color.Unspecified
        } else {
          Color(direction.color)
        }
      }

  PriceSection(
      modifier = modifier,
      value = position.currentValue,
      valueStyle = sizes.title.copy(color = composeColor),
      changeAmount = position.displayGainLossAmount,
      changePercent = position.displayGainLossPercent,
      changeStyle = sizes.description.copy(color = composeColor),
  )
}

@Composable
private fun QuoteScope.PurchaseDate(
    modifier: Modifier = Modifier,
    position: DbPosition,
    clock: Clock,
) {
  val displayPurchaseDate =
      remember(position) { position.purchaseDate.format(DATE_FORMATTER.get().requireNotNull()) }

  Column(
      modifier = modifier,
  ) {
    Info(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.typography),
        value = displayPurchaseDate,
    )

    PurchaseDateTag(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        purchaseDate = position.purchaseDate,
        now = LocalDate.now(clock),
    )
  }
}

@Composable
private fun QuoteScope.CostBasis(
    modifier: Modifier = Modifier,
    position: PositionStock,
) {
  val displayOriginalPrice =
      remember(position) {
        val price = position.price
        val p = if (position.isOption) (price.value * 100).asMoney() else price
        return@remember p.display
      }

  val displayAdjustedPrice =
      remember(position) {
        val price = position.priceWithSplits(position.splits)
        val p = if (position.isOption) (price.value * 100).asMoney() else price
        return@remember p.display
      }

  val prefix =
      remember(
          displayAdjustedPrice,
          displayOriginalPrice,
      ) {
        if (displayAdjustedPrice != displayOriginalPrice) {
          // Something has undergone a split, show adjusted prices
          return@remember "Adjusted "
        } else {
          // No splits, normal prices
          return@remember ""
        }
      }

  Column(
      modifier = modifier,
  ) {
    if (prefix.isNotBlank()) {
      Info(
          name = "Original Cost Basis",
          value = displayOriginalPrice,
      )
    }

    Info(
        name = "${prefix}Cost Basis",
        value = displayAdjustedPrice,
    )

    Info(
        name = "Total ${if (position.isOption && position.isSell) "Premium" else "Cost"}",
        value = position.cost,
    )
  }
}

@Composable
private fun QuoteScope.NumberOfShares(
    modifier: Modifier = Modifier,
    position: PositionStock,
) {
  val displayOriginalShares =
      remember(
          position,
      ) {
        val shareCount = position.shareCount
        val s = if (position.isSell) (shareCount.value * -1).asShares() else shareCount
        return@remember s.display
      }

  val displayAdjustedShares =
      remember(
          position,
      ) {
        val shareCount = position.shareCountWithSplits(position.splits)
        val s = if (position.isSell) (shareCount.value * -1).asShares() else shareCount
        return@remember s.display
      }

  val prefix =
      remember(
          displayAdjustedShares,
          displayOriginalShares,
      ) {
        if (displayAdjustedShares != displayOriginalShares) {
          // Something has undergone a split, show adjusted prices
          return@remember "Adjusted "
        } else {
          // No splits, normal prices
          return@remember ""
        }
      }

  Column(
      modifier = modifier,
  ) {
    if (prefix.isNotBlank()) {
      Info(
          name = "Original ${if (position.isOption) "Contracts" else "Shares"}",
          value = displayOriginalShares,
      )
    }
    Info(
        name = "${prefix}${if (position.isOption) "Contracts" else "Shares"}",
        value = displayAdjustedShares,
    )
  }
}

@Preview
@Composable
private fun PreviewPositionItem() {
  val clock = TestClock

  PositionItem(
      modifier = Modifier.padding(16.dp),
      position =
          PositionStock(
              position = newTestPosition(clock),
              equityType = EquityType.STOCK,
              tradeSide = TradeSide.BUY,
              currentPrice = 25.0.asMoney(),
              splits = emptyList(),
          ),
      clock = clock,
  )
}
