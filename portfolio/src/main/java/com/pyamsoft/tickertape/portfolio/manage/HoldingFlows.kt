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

package com.pyamsoft.tickertape.portfolio.manage

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue

data class HoldingViewState(
    val isLoading: Boolean,
    val stock: PortfolioStock?,
    val numberOfShares: Int,
    val pricePerShare: StockMoneyValue,
) : UiViewState

sealed class HoldingViewEvent : UiViewEvent {

  object ListPositions : HoldingViewEvent()

  object Commit : HoldingViewEvent()

  object ForceRefresh : HoldingViewEvent()

  data class Remove internal constructor(val index: Int) : HoldingViewEvent()

  data class UpdateNumberOfShares internal constructor(val number: Int) : HoldingViewEvent()

  data class UpdateSharePrice internal constructor(val price: StockMoneyValue) : HoldingViewEvent()
}

sealed class HoldingControllerEvent : UiControllerEvent
