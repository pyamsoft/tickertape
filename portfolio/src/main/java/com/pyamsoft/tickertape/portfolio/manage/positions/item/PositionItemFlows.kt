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

package com.pyamsoft.tickertape.portfolio.manage.positions.item

import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.isOption
import com.pyamsoft.tickertape.db.holding.isSellSide
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asShares

sealed class PositionItemViewState : UiViewState {

  data class Position
  internal constructor(
      val holding: DbHolding,
      val position: DbPosition,
      val currentSharePrice: StockMoneyValue?,
  ) : PositionItemViewState() {

    val total: StockMoneyValue
    val gainLossDisplayString: String
    val gainLossDirection: StockDirection
    val isOption = holding.isOption()
    val positionCost: StockMoneyValue
    val positionSize: StockShareValue

    init {
      val optionsModifier = if (isOption) 100 else 1
      val sellSideModifier = if (holding.isSellSide()) -1 else 1

      val numberOfShares = position.shareCount().value()
      val isNoPosition = numberOfShares.compareTo(0) == 0
      val costNumber = position.price().value() * numberOfShares
      total = (costNumber * optionsModifier * sellSideModifier).asMoney()

      val price = currentSharePrice
      if (price == null) {
        gainLossDisplayString = ""
        gainLossDirection = StockDirection.none()
      } else {
        val todayNumber = price.value() * numberOfShares
        val gainLossNumber = todayNumber - costNumber
        val gainLossPercent = gainLossNumber / costNumber * 100
        val isNoChange = gainLossNumber.compareTo(0) == 0
        val direction =
            if (isNoChange) StockDirection.none()
            else (gainLossNumber * sellSideModifier).asDirection()
        val sign = direction.sign()
        gainLossDisplayString =
            "${sign}${(gainLossNumber * sellSideModifier).asMoney().asMoneyValue()} (${sign}${(gainLossPercent * sellSideModifier).asPercent().asPercentValue()})"
        gainLossDirection = direction
      }

      positionCost =
          if (isNoPosition) StockMoneyValue.none() else (costNumber * sellSideModifier).asMoney()
      positionSize =
          if (isNoPosition) StockShareValue.none()
          else (numberOfShares * sellSideModifier).asShares()
    }
  }

  data class Footer
  internal constructor(
      val isOption: Boolean,
      val totalShares: StockShareValue,
      val averageCost: StockMoneyValue,
      val totalCost: StockMoneyValue
  ) : PositionItemViewState()
}

sealed class PositionItemViewEvent : UiViewEvent {

  object Remove : PositionItemViewEvent()
}
