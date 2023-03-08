package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.stocks.api.EquityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import javax.inject.Inject

@Stable
interface PositionAddViewState : UiViewState {
  val equityType: StateFlow<EquityType>
  val isSubmitting: StateFlow<Boolean>
  val isSubmittable: StateFlow<Boolean>
  val pricePerShare: StateFlow<String>
  val numberOfShares: StateFlow<String>
  val dateOfPurchase: StateFlow<LocalDate?>
}

@Stable
class MutablePositionAddViewState
@Inject
internal constructor(
    params: PositionParams,
) : PositionAddViewState {
  override val equityType = MutableStateFlow(params.holdingType)
  override val isSubmitting = MutableStateFlow(false)
  override val isSubmittable = MutableStateFlow(false)
  override val pricePerShare = MutableStateFlow("")
  override val numberOfShares = MutableStateFlow("")
  override val dateOfPurchase = MutableStateFlow<LocalDate?>(null)
}
