package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface SplitAddViewState : UiViewState {
  val isSubmitting: StateFlow<Boolean>
  val isSubmittable: StateFlow<Boolean>
  val preSplitShareCount: StateFlow<String>
  val postSplitShareCount: StateFlow<String>
  val splitDate: StateFlow<LocalDate?>
}

@Stable
class MutableSplitAddViewState @Inject internal constructor() : SplitAddViewState {
  override val isSubmitting = MutableStateFlow(false)
  override val isSubmittable = MutableStateFlow(false)
  override val preSplitShareCount = MutableStateFlow("")
  override val postSplitShareCount = MutableStateFlow("")
  override val splitDate = MutableStateFlow<LocalDate?>(null)
}
