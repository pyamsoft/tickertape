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

package com.pyamsoft.tickertape.portfolio.manage.positions.add

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockShareValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol

data class PositionsAddViewState(
    val symbol: StockSymbol,
    val numberOfShares: StockShareValue,
    val pricePerShare: StockMoneyValue,
) : UiViewState

sealed class PositionsAddViewEvent : UiViewEvent {

  object Close : PositionsAddViewEvent()

  object Commit : PositionsAddViewEvent()

  data class UpdateNumberOfShares internal constructor(val number: StockShareValue) :
      PositionsAddViewEvent()

  data class UpdateSharePrice internal constructor(val price: StockMoneyValue) :
      PositionsAddViewEvent()
}

sealed class PositionsAddControllerEvent : UiControllerEvent