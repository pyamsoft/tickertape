package com.pyamsoft.tickertape.quote.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import com.pyamsoft.tickertape.stocks.api.TradeSide
import javax.inject.Inject

interface NewTickerViewState : UiViewState {
  val isLookup: Boolean
  val isSubmitting: Boolean
  val symbol: String
  val equityType: EquityType?
  val tradeSide: TradeSide
  val lookupError: Throwable?
  val lookupResults: List<SearchResult>
}

internal class MutableNewTickerViewState @Inject internal constructor() : NewTickerViewState {
  override var isLookup by mutableStateOf(false)
  override var isSubmitting by mutableStateOf(false)
  override var equityType by mutableStateOf<EquityType?>(null)
  override var tradeSide by mutableStateOf(TradeSide.BUY)
  override var symbol by mutableStateOf("")
  override var lookupError by mutableStateOf<Throwable?>(null)
  override var lookupResults by mutableStateOf(emptyList<SearchResult>())
}
