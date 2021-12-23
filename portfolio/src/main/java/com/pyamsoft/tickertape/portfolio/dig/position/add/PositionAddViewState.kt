package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.position.DbPosition
import java.time.LocalDate
import javax.inject.Inject

interface PositionAddViewState : UiViewState {
  val positionId: DbPosition.Id
  val isSubmitting: Boolean
  val isSubmittable: Boolean
  val pricePerShare: String
  val numberOfShares: String
  val dateOfPurchase: LocalDate?
}

internal class MutablePositionAddViewState @Inject internal constructor() : PositionAddViewState {
  // This is a constant for the life of the VM
  override var positionId = DbPosition.Id(IdGenerator.generate())
    private set

  override var isSubmitting by mutableStateOf(false)
  override var isSubmittable by mutableStateOf(false)
  override var pricePerShare by mutableStateOf("")
  override var numberOfShares by mutableStateOf("")
  override var dateOfPurchase by mutableStateOf<LocalDate?>(null)

  fun newPosition() {
    positionId = DbPosition.Id(IdGenerator.generate())
  }
}
