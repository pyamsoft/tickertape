package com.pyamsoft.tickertape.portfolio.dig.position.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import java.time.LocalDateTime
import javax.inject.Inject

interface PositionAddViewState : UiViewState {
  val isSubmitting: Boolean
  val isSubmittable: Boolean
  val pricePerShare: String
  val numberOfShares: String
  val dateOfPurchase: LocalDateTime?
}

internal class MutablePositionAddViewState @Inject internal constructor() : PositionAddViewState {
  override var isSubmitting by mutableStateOf(false)
  override var isSubmittable by mutableStateOf(false)
  override var pricePerShare by mutableStateOf("")
  override var numberOfShares by mutableStateOf("")
  override var dateOfPurchase by mutableStateOf<LocalDateTime?>(null)

  @CheckResult
  fun pricePerShareNumber(): Double? {
    return textToDecimalNumber(pricePerShare)
  }

  @CheckResult
  fun numberOfSharesNumber(): Double? {
    return textToDecimalNumber(numberOfShares)
  }

  companion object {

    // Removes anything that isn't a number or the decimal point
    private val DECIMAL_NUMBER_REGEX = Regex("[^0-9.]")

    @JvmStatic
    @CheckResult
    private fun textToDecimalNumber(text: String): Double? {
      return text.trim().replace(DECIMAL_NUMBER_REGEX, "").toDoubleOrNull()
    }
  }
}
