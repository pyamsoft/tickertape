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
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asDirection
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.stocks.api.asShares

sealed class PositionItemViewState : UiViewState {

  data class Position
  internal constructor(
      val holding: DbHolding,
      private val position: DbPosition,
      private val currentSharePrice: StockMoneyValue?,
  ) : PositionItemViewState() {

    val id = position.id()
    val total: StockMoneyValue
    val current: StockMoneyValue
    val gainLossDisplayString: String
    val gainLossDirection: StockDirection

    val isOption = holding.type() == EquityType.OPTION
    val positionCost: StockMoneyValue
    val positionSize: StockShareValue
    val purchaseDate = position.purchaseDate()

    init {
      val optionsModifier = if (isOption) 100 else 1
      val sellSideModifier = if (holding.side() == TradeSide.SELL) -1 else 1

      val numberOfShares = position.shareCount().value()
      val positionPrice = position.price().value()
      val costNumber = positionPrice * numberOfShares
      total = (costNumber * optionsModifier * sellSideModifier).asMoney()

      val price = currentSharePrice
      if (price == null) {
        gainLossDisplayString = ""
        gainLossDirection = StockDirection.none()
        current = StockMoneyValue.none()
      } else {
        val todayNumber = price.value() * numberOfShares
        val gainLossNumber = todayNumber - costNumber
        val gainLossPercent = gainLossNumber / costNumber * 100
        val isNoChange = gainLossNumber.isZero()
        val direction =
            if (isNoChange) StockDirection.none()
            else (gainLossNumber * sellSideModifier).asDirection()
        val sign = direction.sign()
        current = (price.value() * optionsModifier * sellSideModifier).asMoney()
        gainLossDisplayString =
            "${sign}${(gainLossNumber * sellSideModifier).asMoney().asMoneyValue()} (${sign}${(gainLossPercent * sellSideModifier).asPercent().asPercentValue()})"
        gainLossDirection = direction
      }

      val isNoPosition = numberOfShares.isZero()
      if (isNoPosition) {
        positionCost = StockMoneyValue.none()
        positionSize = StockShareValue.none()
      } else {
        positionCost = (positionPrice * sellSideModifier).asMoney()
        positionSize = (numberOfShares * sellSideModifier).asShares()
      }
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
