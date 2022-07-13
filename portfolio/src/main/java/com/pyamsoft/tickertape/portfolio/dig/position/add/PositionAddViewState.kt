package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.stocks.api.EquityType
import java.time.LocalDate
import javax.inject.Inject
import timber.log.Timber

interface PositionAddViewState : UiViewState {
  val equityType: EquityType
  val positionId: DbPosition.Id
  val isSubmitting: Boolean
  val isSubmittable: Boolean
  val pricePerShare: String
  val numberOfShares: String
  val dateOfPurchase: LocalDate?
}

internal class MutablePositionAddViewState
@Inject
internal constructor(
    override val equityType: EquityType,
    private val existingPositionId: DbPosition.Id,
) : PositionAddViewState {
  override var isSubmitting by mutableStateOf(false)
  override var isSubmittable by mutableStateOf(false)
  override var pricePerShare by mutableStateOf("")
  override var numberOfShares by mutableStateOf("")
  override var dateOfPurchase by mutableStateOf<LocalDate?>(null)

  override var positionId by mutableStateOf(decideInitialPositionId())
    // Private set because we don't want someone changing this outside of the newPosition() function
    private set

  // Written to by the VM internally, not exposed to the View
  internal var existingPosition: DbPosition? by mutableStateOf(null)

  @CheckResult
  private fun decideInitialPositionId(): DbPosition.Id {
    return if (existingPositionId.isEmpty()) {
      generateNewPositionId().also { Timber.d("Initial position ID created: $it") }
    } else {
      existingPositionId.also { Timber.d("Initial existing position ID: $it") }
    }
  }

  fun newPosition() {
    if (existingPositionId.isEmpty()) {
      positionId = generateNewPositionId()
    } else {
      throw IllegalStateException("Do not use newPosition() with existing ID: $existingPositionId")
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun generateNewPositionId(): DbPosition.Id {
      return DbPosition.Id(IdGenerator.generate())
    }
  }
}
