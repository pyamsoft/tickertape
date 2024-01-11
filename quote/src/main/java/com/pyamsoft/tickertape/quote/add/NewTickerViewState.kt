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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

@Stable
interface NewTickerViewState : UiViewState {
  val isSubmitting: StateFlow<Boolean>

  val symbol: StateFlow<String>

  val optionExpirationDate: StateFlow<LocalDate?>
  val optionStrikePrice: StateFlow<StockMoneyValue?>
  val optionType: StateFlow<StockOptions.Contract.Type>

  val equityType: StateFlow<EquityType?>
  val tradeSide: StateFlow<TradeSide>

  val resolvedTicker: StateFlow<Ticker?>
  val resolvedOption: StateFlow<StockOptions?>

  val lookupError: StateFlow<Throwable?>
  val lookupResults: StateFlow<List<SearchResult>>

  val canSubmit: Flow<Boolean>
}

@Stable
class MutableNewTickerViewState @Inject internal constructor() : NewTickerViewState {
  internal val validSymbol = MutableStateFlow<StockSymbol?>(null)

  override val isSubmitting = MutableStateFlow(false)
  override val symbol = MutableStateFlow("")

  override val equityType = MutableStateFlow<EquityType?>(null)
  override val tradeSide = MutableStateFlow(TradeSide.BUY)

  override val optionExpirationDate = MutableStateFlow<LocalDate?>(null)
  override val optionStrikePrice = MutableStateFlow<StockMoneyValue?>(null)
  override val optionType = MutableStateFlow(StockOptions.Contract.Type.CALL)

  override val resolvedTicker = MutableStateFlow<Ticker?>(null)
  override val resolvedOption = MutableStateFlow<StockOptions?>(null)

  override val lookupError = MutableStateFlow<Throwable?>(null)
  override val lookupResults = MutableStateFlow(emptyList<SearchResult>())

  override val canSubmit =
      combine(
          symbol,
          validSymbol,
          equityType,
          optionExpirationDate,
          optionStrikePrice,
      ) { args ->
        val symbolValue = args[0] as String
        val validSymbolValue = args[1] as StockSymbol?
        val equityTypeValue = args[2] as EquityType?
        val optionExpirationDateValue = args[3] as LocalDate?
        val optionStrikePriceValue = args[4] as StockMoneyValue?

        if (symbolValue.isBlank()) {
          Timber.w("Cannot submit, blank symbol")
          return@combine false
        }

        if (validSymbolValue == null) {
          Timber.w("Cannot submit, invalid symbol")
          return@combine false
        }

        if (equityTypeValue == null) {
          Timber.w("Cannot submit, invalid type")
          return@combine false
        }

        if (equityTypeValue == EquityType.OPTION) {
          if (optionExpirationDateValue == null) {
            Timber.w("Cannot submit, invalid Option Expiration")
            return@combine false
          }

          if (optionStrikePriceValue == null) {
            Timber.w("Cannot submit, invalid Option Strike")
            return@combine false
          }
        }

        return@combine true
      }
}

@Stable
@Immutable
object InvalidLookupException : IllegalArgumentException("Invalid lookup expression")
