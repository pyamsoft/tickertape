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

package com.pyamsoft.tickertape.main

import android.app.Activity
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.fromJson
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewModeler
@Inject
internal constructor(
    override val state: MutableMainViewState,
    private val theming: Theming,
    private val mainActionSelectionBus: EventBus<MainSelectionEvent>,
    private val jsonParser: JsonParser,
    private val holdingQueryDao: HoldingQueryDao,
) : AbstractViewModeler<MainViewState>(state) {

  fun handleSyncDarkTheme(activity: Activity) {
    val isDark = theming.isDarkTheme(activity)
    state.theme.value = if (isDark) Theming.Mode.DARK else Theming.Mode.LIGHT
  }

  fun handleMainActionSelected(scope: CoroutineScope, page: TopLevelMainPage) {
    scope.launch(context = Dispatchers.Main) {
      mainActionSelectionBus.send(MainSelectionEvent(page = page))
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_THEME) { s.theme.value.name }.also { add(it) }
        registry
            .registerProvider(KEY_PORTFOLIO_DIG) {
              s.portfolioDigParams.value
                  ?.let {
                    DigSavedState(
                        symbol = it.symbol,
                        lookupSymbol = it.lookupSymbol,
                    )
                  }
                  ?.let { jsonParser.toJson(it) }
            }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(KEY_THEME)
        ?.let { it as String }
        ?.let { Theming.Mode.valueOf(it) }
        ?.also { s.theme.value = it }

    registry
        .consumeRestored(KEY_PORTFOLIO_DIG)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<DigSavedState>(it) }
        ?.also { saved ->
          handleOpenDig(
              // TODO: Can we avoid making a random CoroutineScope here?
              scope = MainScope(),
              symbol = saved.symbol,
              lookupSymbol = saved.lookupSymbol,
          )
        }
  }

  fun handleOpenDig(scope: CoroutineScope, ticker: Ticker) {
    val quote = ticker.quote
    if (quote == null) {
      Timber.w("Can't show dig dialog, missing quote: $ticker")
      return
    }

    handleOpenDig(
        scope = scope,
        symbol = quote.symbol,
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
            lookupSymbol = lookupSymbol,
            holding = holding,
            currentPrice = currentPrice,
        )
  }

  fun handleOpenDig(
      scope: CoroutineScope,
      symbol: StockSymbol,
      lookupSymbol: StockSymbol?,
      currentPrice: StockMoneyValue? = null
  ) {
    scope.launch(context = Dispatchers.Main) {
      val holding = holdingQueryDao.query().firstOrNull { it.symbol == symbol }
      if (holding == null) {
        Timber.w("Can't show dig dialog, missing holding: $symbol $lookupSymbol")
        return@launch
      }

      state.portfolioDigParams.value =
          PortfolioDigParams(
              symbol = symbol,
              lookupSymbol = lookupSymbol,
              holding = holding,
              currentPrice = currentPrice,
          )
    }
  }

  fun handleCloseDig() {
    state.portfolioDigParams.value = null
  }

  companion object {

    private const val KEY_THEME = "theme"
    private const val KEY_PORTFOLIO_DIG = "portfolio_dig"
  }

  @JsonClass(generateAdapter = true)
  private data class DigSavedState(
      val symbol: StockSymbol,
      val lookupSymbol: StockSymbol?,
  )
}
