package com.pyamsoft.tickertape.quote.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.SearchResult
import javax.inject.Inject

interface NewTickerViewState : UiViewState {
  val symbol: String
  val equityType: EquityType?
  val isLookup: Boolean
  val lookupError: Throwable?
  val lookupResults: List<SearchResult>
}

internal class MutableNewTickerViewState @Inject internal constructor() : NewTickerViewState {
  override var equityType by mutableStateOf<EquityType?>(null)
  override var symbol by mutableStateOf("")
  override var isLookup by mutableStateOf(false)
  override var lookupError by mutableStateOf<Throwable?>(null)
  override var lookupResults by mutableStateOf(emptyList<SearchResult>())
}
