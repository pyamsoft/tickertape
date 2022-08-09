package com.pyamsoft.tickertape.portfolio.dig.splits.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.core.IdGenerator
import com.pyamsoft.tickertape.db.split.DbSplit
import java.time.LocalDate
import javax.inject.Inject
import timber.log.Timber

@Stable
interface SplitAddViewState : UiViewState {
  val splitId: DbSplit.Id
  val isSubmitting: Boolean
  val isSubmittable: Boolean
  val preSplitShareCount: String
  val postSplitShareCount: String
  val splitDate: LocalDate?
}

@Stable
internal class MutableSplitAddViewState
@Inject
internal constructor(
    private val existingSplitId: DbSplit.Id,
) : SplitAddViewState {
  override var isSubmitting by mutableStateOf(false)
  override var isSubmittable by mutableStateOf(false)
  override var preSplitShareCount by mutableStateOf("")
  override var postSplitShareCount by mutableStateOf("")
  override var splitDate by mutableStateOf<LocalDate?>(null)

  override var splitId by mutableStateOf(decideInitialSplitId())
    // Private set because we don't want someone changing this outside of the newPosition() function
    private set

  // Written to by the VM internally, not exposed to the View
  internal var existingSplit: DbSplit? by mutableStateOf(null)

  @CheckResult
  private fun decideInitialSplitId(): DbSplit.Id {
    return if (existingSplitId.isEmpty) {
      generateNewSplitId().also { Timber.d("Initial split ID created: $it") }
    } else {
      existingSplitId.also { Timber.d("Initial existing split ID: $it") }
    }
  }

  fun newSplit() {
    if (existingSplitId.isEmpty) {
      splitId = generateNewSplitId()
    } else {
      throw IllegalStateException("Do not use newPosition() with existing ID: $existingSplitId")
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun generateNewSplitId(): DbSplit.Id {
      return DbSplit.Id(IdGenerator.generate())
    }
  }
}
