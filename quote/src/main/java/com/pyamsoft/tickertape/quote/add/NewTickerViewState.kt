package com.pyamsoft.tickertape.quote.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptions
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import java.time.LocalDate
import javax.inject.Inject

interface NewTickerViewState : UiViewState {
  val isLookup: Boolean

  val symbol: String

  val optionExpirationDate: LocalDate?
  val optionStrikePrice: StockMoneyValue?
  val optionType: StockOptions.Contract.Type?

  val equityType: EquityType?
  val tradeSide: TradeSide

  val lookupError: Throwable?
  val lookupResults: List<SearchResult>

  @CheckResult fun canSubmit(): Boolean
}

internal class MutableNewTickerViewState @Inject internal constructor() : NewTickerViewState {
  internal var isSubmitting by mutableStateOf(false)
  internal var validSymbol by mutableStateOf<StockSymbol?>(null)

  override var isLookup by mutableStateOf(false)

  override var equityType by mutableStateOf<EquityType?>(null)
  override var tradeSide by mutableStateOf(TradeSide.BUY)

  override var symbol by mutableStateOf("")

  override var optionExpirationDate by mutableStateOf<LocalDate?>(null)
  override var optionStrikePrice by mutableStateOf<StockMoneyValue?>(null)
  override var optionType by mutableStateOf<StockOptions.Contract.Type?>(null)

  override var lookupError by mutableStateOf<Throwable?>(null)
  override var lookupResults by mutableStateOf(emptyList<SearchResult>())

  override fun canSubmit(): Boolean {
    return if (isSubmitting || symbol.isBlank() || validSymbol == null) {
      false
    } else if (equityType !== EquityType.OPTION) {
      true
    } else {
      optionExpirationDate != null && optionStrikePrice != null && optionType != null
    }
  }
}

object InvalidLookupException : IllegalArgumentException("Invalid lookup expression")
