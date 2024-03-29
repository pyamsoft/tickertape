/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class NewTickerViewModeler
@Inject
internal constructor(
    override val state: MutableNewTickerViewState,
    private val interactor: NewTickerInteractor,
) : NewTickerViewState by state, AbstractViewModeler<NewTickerViewState>(state) {

  private var symbolLookupJob: Job? = null
  private var symbolResolutionJob: Job? = null
  private var optionLookupJob: Job? = null

  private fun performSymbolResolution(
      scope: CoroutineScope,
      symbol: String,
  ) {
    state.resolvedTicker.value = null

    if (symbol.isBlank()) {
      Timber.w("Cannot resolve for empty symbol")
      return
    }

    val sym = symbol.uppercase()
    symbolResolutionJob?.cancel()
    symbolResolutionJob =
        scope.launch(context = Dispatchers.Default) {
          val s = sym.asSymbol()
          interactor
              .resolveTicker(s)
              .onSuccess { Timber.d("Resolved ticker for $sym $it") }
              .onSuccess { state.resolvedTicker.value = it }
              .onFailure { Timber.e(it, "Error resolving ticker for $sym") }
              .onFailure { state.resolvedTicker.value = null }
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
        lookupError.value = null
        lookupResults.value = emptyList()
      }
      return
    }

    symbolLookupJob?.cancel()
    symbolLookupJob =
        scope.launch(context = Dispatchers.Default) {
          interactor
              .search(symbol)
              .onFailure { Timber.e(it, "Error looking up results for $symbol") }
              .onSuccess { Timber.d("Found search results for $symbol $it") }
              .map { processLookupResults(it) }
              .onSuccess { r ->
                s.apply {
                  lookupError.value = null
                  lookupResults.value = r
                }
              }
              .onSuccess { r ->
                // Auto select a matching symbol if one is exact
                r.firstOrNull { it.symbol == symbol.asSymbol() }
                    ?.also { result ->
                      onSearchResultSelected(
                          scope = scope,
                          symbol = result.symbol,
                          dismiss = false,
                      )
                    }
              }
              .onFailure { e ->
                s.apply {
                  lookupError.value = e
                  lookupResults.value = emptyList()
                }
              }
        }
  }

  @CheckResult
  private fun processLookupResults(results: List<SearchResult>): List<SearchResult> {
    val equityType = state.equityType.value
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
    symbol.value = ""

    // Not null but these are the defaults
    tradeSide.value = TradeSide.BUY
    optionType.value = StockOptions.Contract.Type.CALL

    validSymbol.value = null
    optionStrikePrice.value = null
    optionExpirationDate.value = null
  }

  @CheckResult
  private suspend fun MutableNewTickerViewState.resolveSubmission(): String {
    return if (equityType.value != EquityType.OPTION) {
      symbol.value
    } else {
      interactor.resolveOptionsIdentifier(
          validSymbol.value.requireNotNull(),
          optionExpirationDate.value.requireNotNull(),
          optionStrikePrice.value.requireNotNull(),
          optionType.value.requireNotNull(),
      )
    }
  }

  private fun performLookupOptionData(scope: CoroutineScope, symbol: StockSymbol) {
    val s = state

    optionLookupJob?.cancel()
    optionLookupJob =
        scope.launch(context = Dispatchers.Default) {
          interactor
              .getOptionsChain(symbol, s.optionExpirationDate.value)
              .onFailure { Timber.e(it, "Error looking up options data: ${symbol.raw}") }
              .onSuccess { Timber.d("Options data: $it") }
              .onSuccess { option ->
                s.resolvedOption.value = option

                val strike = s.optionStrikePrice.value
                if (strike != null) {
                  // Clear price if it is not present in Strike price list for current Option
                  // expiration
                  // date
                  if (!option.strikes.map { it.value }.contains(strike.value)) {
                    s.optionStrikePrice.value = null
                  }
                }
              }
              .onFailure { s.resolvedOption.value = null }
        }
  }

  private fun MutableNewTickerViewState.dismissSearchResultsPopup() {
    lookupResults.value = emptyList()
    lookupError.value = null
  }

  private fun onSearchResultSelected(
      scope: CoroutineScope,
      symbol: StockSymbol,
      dismiss: Boolean,
  ) {
    val s = state
    s.apply {
      Timber.d("Found new valid symbol: $symbol")
      validSymbol.value = symbol
      this.symbol.value = symbol.raw

      if (dismiss) {
        dismissSearchResultsPopup()
      }
    }

    if (s.equityType.value == EquityType.OPTION) {
      performLookupOptionData(scope, symbol)
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_SYMBOL) { s.symbol.value }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry.consumeRestored(KEY_SYMBOL)?.let { it as String }?.also { s.symbol.value = it }
  }

  fun dispose() {
    symbolResolutionJob?.cancel()
    symbolResolutionJob = null

    symbolLookupJob?.cancel()
    symbolLookupJob = null

    optionLookupJob?.cancel()
    optionLookupJob = null
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
      this.symbol.value = symbol
      validSymbol.value = null
    }
  }

  fun handleEquityTypeSelected(type: EquityType) {
    state.apply {
      equityType.value = type
      clearInput()
    }
  }

  fun handleClearEquityType() {
    state.apply {
      equityType.value = null
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

  fun handleOptionExpirationDate(scope: CoroutineScope, date: LocalDate) {
    val s = state

    s.optionExpirationDate.value = date

    // Retrigger options lookup for new expiration date
    val symbol = s.validSymbol.value
    if (s.equityType.value == EquityType.OPTION && symbol != null) {
      performLookupOptionData(scope, symbol)
    }
  }

  fun handleOptionStrikePrice(price: StockMoneyValue) {
    state.optionStrikePrice.value = price
  }

  fun handleOptionType(type: StockOptions.Contract.Type) {
    state.optionType.value = type
  }

  fun handleDismiss() {
    handleClearEquityType()
    handleClear()

    state.isSubmitting.value = false
  }

  fun handleClear() {
    val s = state
    s.clearInput()

    s.lookupResults.value = emptyList()
    s.lookupError.value = null

    s.resolvedOption.value = null
    s.resolvedTicker.value = null
  }

  fun handleSubmit(scope: CoroutineScope) {
    val s = state
    if (s.isSubmitting.value) {
      Timber.w("Already submitting, don't double up")
      return
    }

    s.apply {
      scope.launch(context = Dispatchers.Default) {
        if (!s.canSubmit.first()) {
          Timber.w("Cannot process submit")
          return@launch
        }

        if (s.isSubmitting.value) {
          Timber.w("Already submitting, don't double up")
          return@launch
        }

        s.isSubmitting.value = true
        val sym = resolveSubmission()

        // If blank, we can't do anything
        if (sym.isBlank()) {
          Timber.w(
              "Invalid lookup symbol generated: $symbol $validSymbol $optionExpirationDate $optionStrikePrice $optionType")
          s.isSubmitting.value = false
          throw InvalidLookupException
        }

        interactor
            .insertNewTicker(
                symbol = sym.asSymbol(),
                equityType = s.equityType.value.requireNotNull(),
                tradeSide = s.tradeSide.value,
            )
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
            .onFinally { s.isSubmitting.value = false }
            .onSuccess { handleClear() }
      }
    }
  }

  fun handleTradeSideChanged(side: TradeSide) {
    state.tradeSide.value = side
  }

  companion object {
    private const val KEY_SYMBOL = "key_symbol"
  }
}
