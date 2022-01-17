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
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDate
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

  private val optionLookupRunner =
      highlander<ResultWrapper<StockOptions>, Boolean, StockSymbol> { force, symbol ->
        interactor.lookupOptionsData(
            force = force,
            symbol = symbol,
        )
      }

  private val optionResolverRunner =
      highlander<String, StockSymbol, LocalDate, StockMoneyValue, StockOptions.Contract.Type> {
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
      highlander<ResultWrapper<DbInsert.InsertResult<StockSymbol>>, String> {
        val s = state
        interactor.insertNewTicker(
            symbol = it.asSymbol(),
            destination = destination,
            equityType = s.equityType.requireNotNull(),
            tradeSide = s.tradeSide,
        )
      }

  private val lookupRunner =
      highlander<ResultWrapper<List<SearchResult>>, Boolean, String> { force, query ->
        interactor.search(force, query)
      }

  private fun performSymbolLookup(scope: CoroutineScope, symbol: String) {
    scope.launch(context = Dispatchers.Main) {
      lookupRunner
          .call(false, symbol)
          .onFailure { Timber.e(it, "Error looking up results for $symbol") }
          .onSuccess { Timber.d("Found search results for $symbol $it") }
          .map { processLookupResults(it) }
          .onSuccess { r ->
            state.apply {
              lookupError = null
              lookupResults = r
            }
          }
          .onFailure { e ->
            state.apply {
              lookupError = e
              lookupResults = emptyList()
            }
          }
          .onFinally { state.apply { isLookup = false } }
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
          EquityType.STOCK -> result.type() == EquityType.STOCK
          // STOCK instead of OPTION since we will use the STOCK to build the OPTION lookup
          EquityType.OPTION -> result.type() == EquityType.STOCK
          EquityType.CRYPTOCURRENCY -> result.type() == EquityType.CRYPTOCURRENCY
        }
      }
    }
  }

  private fun MutableNewTickerViewState.cancelInProgressLookup(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      // Cancel any active runner first
      lookupRunner.cancel()

      // Flip all lookup bits back
      isLookup = false
      lookupError = null
      lookupResults = emptyList()
    }
  }

  private fun MutableNewTickerViewState.clearInput() {
    symbol = ""
    validSymbol = null
    optionType = null
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
          .onFailure { Timber.e(it, "Error looking up options data: ${symbol.symbol()}") }
          .onSuccess { Timber.d("Options data: $it") }
    }
  }

  fun handleSymbolChanged(
      scope: CoroutineScope,
      symbol: String,
  ) {
    state.apply {
      this.symbol = symbol
      validSymbol = null

      cancelInProgressLookup(scope)
    }

    performSymbolLookup(scope, symbol)
  }

  fun handleEquityTypeSelected(
      scope: CoroutineScope,
      type: EquityType,
  ) {
    state.apply {
      equityType = type
      clearInput()
      cancelInProgressLookup(scope)
    }
  }

  fun handleClearEquityType(scope: CoroutineScope) {
    state.apply {
      equityType = null
      clearInput()
      cancelInProgressLookup(scope)
    }
  }

  fun handleSearchResultSelected(
      scope: CoroutineScope,
      result: SearchResult,
  ) {
    val sym = result.symbol()

    val s = state
    s.apply {
      validSymbol = sym
      symbol = sym.symbol()
    }

    if (s.equityType == EquityType.OPTION) {
      performLookupOptionData(scope, sym)
    }
  }

  fun handleClear(scope: CoroutineScope) {
    state.apply {
      clearInput()
      cancelInProgressLookup(scope)
    }
  }

  fun handleOptionExpirationDate(date: LocalDate) {
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
      cancelInProgressLookup(scope)
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
}
