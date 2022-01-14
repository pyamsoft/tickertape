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
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asMoney
import java.time.LocalDate
import javax.inject.Inject

interface NewTickerViewState : UiViewState {
  val isLookup: Boolean
  val isSubmitting: Boolean
  val isValidSymbol: Boolean

  val symbol: String
  val optionExpirationDate: LocalDate?
  val optionStrikePrice: StockMoneyValue?
  val optionType: StockOptions.Contract.Type?

  val equityType: EquityType?
  val tradeSide: TradeSide

  val lookupError: Throwable?
  val lookupResults: List<SearchResult>

  @CheckResult
  fun canSubmit(): Boolean {
    return if (isSubmitting || symbol.isBlank() || !isValidSymbol) {
      false
    } else if (equityType !== EquityType.OPTION) {
      true
    } else {
      optionExpirationDate != null && optionStrikePrice != null && optionType != null
    }
  }
}

internal class MutableNewTickerViewState @Inject internal constructor() : NewTickerViewState {
  override var isLookup by mutableStateOf(false)
  override var isSubmitting by mutableStateOf(false)
  override var isValidSymbol by mutableStateOf(false)

  override var equityType by mutableStateOf<EquityType?>(null)
  override var tradeSide by mutableStateOf(TradeSide.BUY)

  override var symbol by mutableStateOf("")
  override var optionExpirationDate by mutableStateOf<LocalDate?>(LocalDate.now())
  override var optionStrikePrice by mutableStateOf<StockMoneyValue?>(305.0.asMoney())
  override var optionType by
      mutableStateOf<StockOptions.Contract.Type?>(StockOptions.Contract.Type.CALL)

  override var lookupError by mutableStateOf<Throwable?>(null)
  override var lookupResults by mutableStateOf(emptyList<SearchResult>())
}

object InvalidLookupException : IllegalArgumentException("Invalid lookup expression")
