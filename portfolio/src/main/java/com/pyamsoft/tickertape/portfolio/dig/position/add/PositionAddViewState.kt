package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import java.time.LocalDateTime
import javax.inject.Inject

interface PositionAddViewState : UiViewState {
  val pricePerShare: String
  val numberOfShares: String
  val dateOfPurchase: LocalDateTime?
}

internal class MutablePositionAddViewState @Inject internal constructor() : PositionAddViewState {
  override var pricePerShare by mutableStateOf("")
  override var numberOfShares by mutableStateOf("")
  override var dateOfPurchase by mutableStateOf<LocalDateTime?>(null)
}
