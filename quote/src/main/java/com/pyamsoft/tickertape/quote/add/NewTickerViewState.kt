package com.pyamsoft.tickertape.quote.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.stocks.api.EquityType
import javax.inject.Inject

interface NewTickerViewState : UiViewState {
  val equityType: EquityType?
}

internal class MutableNewTickerViewState @Inject internal constructor() : NewTickerViewState {
  override var equityType by mutableStateOf<EquityType?>(null)
}
