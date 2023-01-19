package com.pyamsoft.tickertape.quote.add

import androidx.annotation.CheckResult
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

  // TODO move into vm
  @CheckResult fun canSubmit(): Boolean
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

  override fun canSubmit(): Boolean {
    return if (isSubmitting.value || symbol.value.isBlank() || validSymbol.value == null) {
      false
    } else if (equityType.value != EquityType.OPTION) {
      true
    } else {
      optionExpirationDate.value != null && optionStrikePrice.value != null
    }
  }
}

@Stable
@Immutable
object InvalidLookupException : IllegalArgumentException("Invalid lookup expression")
