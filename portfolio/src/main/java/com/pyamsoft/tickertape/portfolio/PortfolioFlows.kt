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

package com.pyamsoft.tickertape.portfolio

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.TabsSection

// Public constructor, used in home module
data class PortfolioViewState(
    val section: TabsSection,
    val isLoading: Boolean,
    val portfolio: PackedData<PortfolioStockList>,
    val bottomOffset: Int,
) : UiViewState

sealed class PortfolioViewEvent : UiViewEvent {

  object ForceRefresh : PortfolioViewEvent()

  data class Remove internal constructor(val index: Int) : PortfolioViewEvent()

  data class Manage internal constructor(val index: Int) : PortfolioViewEvent()

  object ShowStocks : PortfolioViewEvent()

  object ShowOptions : PortfolioViewEvent()
}

sealed class PortfolioControllerEvent : UiControllerEvent {

  data class AddNewHolding internal constructor(val type: HoldingType) : PortfolioControllerEvent()

  data class ManageHolding internal constructor(val stock: PortfolioStock) :
      PortfolioControllerEvent()
}
