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

package com.pyamsoft.tickertape.quote.add

import androidx.annotation.CheckResult
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class NewTickerViewModeler
@Inject
internal constructor(
    private val state: MutableNewTickerViewState,
    interactor: NewTickerInteractor,
    destination: TickerDestination,
) : AbstractViewModeler<NewTickerViewState>(state) {

  private val tickerResolutionRunner =
      highlander<ResultWrapper<Ticker>, Boolean, String> { force, query ->
        interactor.resolveTicker(
            force = force,
            symbol = query.asSymbol(),
        )
      }

  private val optionLookupRunner =
      highlander<ResultWrapper<StockOptions>, Boolean, StockSymbol> { force, symbol ->
        interactor.lookupOptionsData(
            force = force,
            symbol = symbol,
        )
      }

  private val optionResolverRunner =
      highlander<String, StockSymbol, LocalDateTime, StockMoneyValue, StockOptions.Contract.Type> {
          symbol,
          expirationDate,
          strikePrice,
          contractType ->
        interactor.resolveOptionsIdentifier(
            symbol = symbol,
            expirationDate = expirationDate,
            strikePrice = strikePrice,
            contractType = contractType,
        )
      }

  private val insertRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<StockSymbol>>, String> { symbol ->
        val s = state
        interactor.insertNewTicker(
            symbol = symbol.asSymbol(),
            destination = destination,
            equityType = s.equityType.requireNotNull(),
            tradeSide = s.tradeSide,
        )
      }

  private val symbolLookupRunner =
      highlander<ResultWrapper<List<SearchResult>>, Boolean, String> { force, query ->
        interactor.search(force, query)
      }

  private fun performSymbolResolution(
      scope: CoroutineScope,
      symbol: String,
  ) {
    val s = state
    s.resolvedTicker = null

    if (symbol.isBlank()) {
      Timber.w("Cannot resolve for empty symbol")
      return
    }

    val sym = symbol.uppercase()
    scope.launch(context = Dispatchers.Main) {
      tickerResolutionRunner
          .call(false, sym)
          .onSuccess { Timber.d("Resolved ticker for $sym $it") }
          .onSuccess { s.resolvedTicker = it }
          .onFailure { Timber.e(it, "Error resolving ticker for $sym") }
          .onFailure { s.resolvedTicker = null }
          .onSuccess { ticker ->
            ticker.quote?.also { q ->
              // Auto select the valid symbol if we found a quote for it
              onSearchResultSelected(
                  scope = this,
                  symbol = q.symbol,
                  dismiss = false,
              )
            }
          }
    }
  }

  private fun performSymbolLookup(
      scope: CoroutineScope,
      symbol: String,
  ) {
    val s = state

    if (symbol.isBlank()) {
      Timber.w("Cannot lookup for empty symbol")
      s.apply {
        lookupError = null
        lookupResults = emptyList()
      }
      return
    }

    scope.launch(context = Dispatchers.Main) {
      symbolLookupRunner
          .call(false, symbol)
          .onFailure { Timber.e(it, "Error looking up results for $symbol") }
          .onSuccess { Timber.d("Found search results for $symbol $it") }
          .map { processLookupResults(it) }
          .onSuccess { r ->
            s.apply {
              lookupError = null
              lookupResults = r
            }
          }
          .onSuccess { r ->
            // Auto select a matching symbol if one is exact
            r.firstOrNull { it.symbol == symbol.asSymbol() }?.also { result ->
              onSearchResultSelected(
                  scope = scope,
                  symbol = result.symbol,
                  dismiss = false,
              )
            }
          }
          .onFailure { e ->
            s.apply {
              lookupError = e
              lookupResults = emptyList()
            }
          }
    }
  }

  @CheckResult
  private fun processLookupResults(results: List<SearchResult>): List<SearchResult> {
    val equityType = state.equityType
    return if (equityType == null) {
      emptyList()
    } else {
      results.filter { result ->
        when (equityType) {
          EquityType.STOCK -> result.type == EquityType.STOCK
          // STOCK instead of OPTION since we will use the STOCK to build the OPTION lookup
          EquityType.OPTION -> result.type == EquityType.STOCK
          EquityType.CRYPTOCURRENCY -> result.type == EquityType.CRYPTOCURRENCY
        }
      }
    }
  }

  private fun MutableNewTickerViewState.clearInput() {
    symbol = ""

    // Not null but these are the defaults
    tradeSide = TradeSide.BUY
    optionType = StockOptions.Contract.Type.CALL

    validSymbol = null
    optionStrikePrice = null
    optionExpirationDate = null
  }

  @CheckResult
  private suspend fun MutableNewTickerViewState.resolveSubmission(): String {
    return if (equityType != EquityType.OPTION) {
      symbol
    } else {
      optionResolverRunner.call(
          validSymbol.requireNotNull(),
          optionExpirationDate.requireNotNull(),
          optionStrikePrice.requireNotNull(),
          optionType.requireNotNull(),
      )
    }
  }

  private fun performLookupOptionData(scope: CoroutineScope, symbol: StockSymbol) {
    scope.launch(context = Dispatchers.Main) {
      optionLookupRunner
          .call(false, symbol)
          .onFailure { Timber.e(it, "Error looking up options data: ${symbol.raw}") }
          .onSuccess { Timber.d("Options data: $it") }
          .onSuccess { state.resolvedOption = it }
          .onFailure { state.resolvedOption = null }
    }
  }

  private fun MutableNewTickerViewState.dismissSearchResultsPopup() {
    lookupResults = emptyList()
    lookupError = null
  }

  private fun onSearchResultSelected(
      scope: CoroutineScope,
      symbol: StockSymbol,
      dismiss: Boolean,
  ) {
    val s = state
    s.apply {
      validSymbol = symbol
      this.symbol = symbol.raw

      if (dismiss) {
        dismissSearchResultsPopup()
      }
    }

    if (s.equityType == EquityType.OPTION) {
      performLookupOptionData(scope, symbol)
    }
  }

  fun handleSearchResultsDismissed() {
    state.dismissSearchResultsPopup()
  }

  fun handleAfterSymbolChanged(scope: CoroutineScope, symbol: String) {
    performSymbolLookup(scope = scope, symbol = symbol)
    performSymbolResolution(scope = scope, symbol = symbol)
  }

  fun handleSymbolChanged(symbol: String) {
    state.apply {
      this.symbol = symbol
      validSymbol = null
    }
  }

  fun handleEquityTypeSelected(type: EquityType) {
    state.apply {
      equityType = type
      clearInput()
    }
  }

  fun handleClearEquityType() {
    state.apply {
      equityType = null
      clearInput()
    }
  }

  fun handleSearchResultSelected(
      scope: CoroutineScope,
      result: SearchResult,
  ) {
    // Manually selected so we dismiss the dropdown
    onSearchResultSelected(
        scope = scope,
        symbol = result.symbol,
        dismiss = true,
    )
  }

  fun handleClear() {
    state.clearInput()
  }

  fun handleOptionExpirationDate(date: LocalDateTime) {
    state.optionExpirationDate = date
  }

  fun handleOptionStrikePrice(price: StockMoneyValue) {
    state.optionStrikePrice = price
  }

  fun handleOptionType(type: StockOptions.Contract.Type) {
    state.optionType = type
  }

  fun handleSubmit(
      scope: CoroutineScope,
      onSubmit: () -> Unit,
  ) {
    val s = state
    if (!s.canSubmit()) {
      return
    }

    s.apply {
      s.isSubmitting = true
      scope.launch(context = Dispatchers.Main) {
        val sym = resolveSubmission()

        // If blank, we can't do anything
        if (sym.isBlank()) {
          Timber.w(
              "Invalid lookup symbol generated: $symbol $validSymbol $optionExpirationDate $optionStrikePrice $optionType")
          s.isSubmitting = false
          throw InvalidLookupException
        }

        insertRunner
            .call(sym)
            .onFailure { Timber.e(it, "Failed to insert new symbol: $sym") }
            .onSuccess { result ->
              when (result) {
                is DbInsert.InsertResult.Fail -> {
                  Timber.e(result.error, "Failed to insert new symbol: ${result.data}")
                  throw result.error
                }
                is DbInsert.InsertResult.Insert -> Timber.d("Inserted new symbol: ${result.data}")
                is DbInsert.InsertResult.Update ->
                    Timber.w("UPDATE happened but none was expected: ${result.data}")
              }
            }
            .onSuccess { onSubmit() }
            .onFinally { s.isSubmitting = false }
      }
    }
  }

  fun handleTradeSideChanged(side: TradeSide) {
    state.tradeSide = side
  }
}
