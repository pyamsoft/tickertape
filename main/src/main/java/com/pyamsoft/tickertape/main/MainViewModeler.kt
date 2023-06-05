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

package com.pyamsoft.tickertape.main

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.tickertape.core.ActivityScope
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@ActivityScope
class MainViewModeler
@Inject
internal constructor(
    override val state: MutableMainViewState,
    private val mainActionSelectionBus: EventBus<MainSelectionEvent>,
    private val jsonParser: JsonParser,
) : AbstractViewModeler<MainViewState>(state) {

  fun handleMainActionSelected(scope: CoroutineScope, page: MainPage) {
    val event = MainSelectionEvent(page = page)
    scope.launch(context = Dispatchers.Default) { mainActionSelectionBus.emit(event) }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry
            .registerProvider(KEY_PORTFOLIO_DIG) {
              s.portfolioDigParams.value?.let { jsonParser.toJson(it.toJson()) }
            }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_PORTFOLIO_DIG)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<PortfolioDigParams.Json>(it) }
        ?.fromJson()
        ?.also { saved ->
          handleOpenDig(
              symbol = saved.symbol,
              equityType = saved.equityType,
              lookupSymbol = saved.lookupSymbol,
              currentPrice = saved.currentPrice,
          )
        }
  }

  fun handleOpenDig(ticker: Ticker) {
    val quote = ticker.quote
    if (quote == null) {
      Timber.w("Can't show dig dialog, missing quote: $ticker")
      return
    }

    handleOpenDig(
        symbol = quote.symbol,
        equityType = quote.type,
        lookupSymbol = if (quote is StockOptionsQuote) quote.underlyingSymbol else quote.symbol,
        currentPrice = quote.currentSession.price,
    )
  }

  fun handleOpenDig(
      holding: DbHolding,
      lookupSymbol: StockSymbol?,
      currentPrice: StockMoneyValue? = null
  ) {
    state.portfolioDigParams.value =
        PortfolioDigParams(
            symbol = holding.symbol,
            equityType = holding.type,
            lookupSymbol = lookupSymbol,
            currentPrice = currentPrice,
        )
  }

  fun handleOpenDig(
      symbol: StockSymbol,
      equityType: EquityType,
      lookupSymbol: StockSymbol?,
      currentPrice: StockMoneyValue? = null
  ) {
    state.portfolioDigParams.value =
        PortfolioDigParams(
            symbol = symbol,
            equityType = equityType,
            lookupSymbol = lookupSymbol,
            currentPrice = currentPrice,
        )
  }

  fun handleCloseDig() {
    state.portfolioDigParams.value = null
  }

  companion object {

    private const val KEY_PORTFOLIO_DIG = "portfolio_dig"
  }
}
