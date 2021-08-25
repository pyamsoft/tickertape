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
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.ui.PackedData
import com.pyamsoft.tickertape.ui.pack
import com.pyamsoft.tickertape.ui.packError

// Public constructor, used in home module
data class PortfolioViewState(
    val query: String,
    val section: PortfolioTabSection,
    val isLoading: Boolean,
    val portfolio: PackedData<List<PortfolioStock>>,
    val topOffset: Int,
    val bottomOffset: Int,
) : UiViewState {

  val stockList: PackedData<PortfolioStockList> =
      when (val p = portfolio) {
        is PackedData.Data -> PortfolioStockList(p.value).pack()
        is PackedData.Error -> p.throwable.packError()
      }

  val displayPortfolio =
      when (val p = portfolio) {
        is PackedData.Data -> {
          val list = p.value
          val header =
              if (section == PortfolioTabSection.STOCK) {
                listOf(
                    DisplayPortfolio.Header(
                        query,
                        section,
                        isLoading,
                        portfolio,
                        topOffset,
                        bottomOffset,
                    ))
              } else {
                emptyList()
              }

          val currentSearch = query
          val allItems =
              header +
                  list
                      .asSequence()
                      .filter { qs ->
                        val symbol = qs.holding.symbol().symbol()
                        val name = qs.quote?.quote?.company()?.company()
                        return@filter if (symbol.contains(currentSearch, ignoreCase = true)) true
                        else name?.contains(currentSearch, ignoreCase = true) ?: false
                      }
                      .map { DisplayPortfolio.Item(it) }
          allItems.pack()
        }
        is PackedData.Error -> p.throwable.packError()
      }

  sealed class DisplayPortfolio {

    data class Header
    internal constructor(
        val query: String,
        val section: PortfolioTabSection,
        val isLoading: Boolean,
        val portfolio: PackedData<List<PortfolioStock>>,
        val topOffset: Int,
        val bottomOffset: Int,
    ) : DisplayPortfolio()

    data class Item internal constructor(val stock: PortfolioStock) : DisplayPortfolio()
  }
}

sealed class PortfolioViewEvent : UiViewEvent {

  object ForceRefresh : PortfolioViewEvent()

  data class Search internal constructor(val query: String) : PortfolioViewEvent()

  data class Remove internal constructor(val index: Int) : PortfolioViewEvent()

  data class Manage internal constructor(val index: Int) : PortfolioViewEvent()

  object ShowStocks : PortfolioViewEvent()

  object ShowOptions : PortfolioViewEvent()

  object ShowCrypto : PortfolioViewEvent()
}

sealed class PortfolioControllerEvent : UiControllerEvent {

  data class AddNewHolding
  internal constructor(
      val type: EquityType,
      val side: TradeSide,
  ) : PortfolioControllerEvent()

  data class ManageHolding internal constructor(val stock: PortfolioStock) :
      PortfolioControllerEvent()
}
