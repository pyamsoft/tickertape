package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.position.PositionStock
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.SplitParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@Stable
interface BasePortfolioDigViewState : BaseDigViewState {
  val section: StateFlow<PortfolioDigSections>

  val holding: StateFlow<DbHolding?>
  val holdingError: StateFlow<Throwable?>
}

@Stable
interface PositionsPortfolioDigViewState : BasePortfolioDigViewState {
  val positions: StateFlow<List<PositionStock>>
  val positionsError: StateFlow<Throwable?>
}

@Stable
interface SplitsPortfolioDigViewState : BasePortfolioDigViewState {
  val stockSplits: StateFlow<List<DbSplit>>
  val stockSplitError: StateFlow<Throwable?>
}

@Stable
interface PortfolioDigViewState :
    DigViewState, PositionsPortfolioDigViewState, SplitsPortfolioDigViewState {
  val splitDialog: StateFlow<SplitParams?>
}

// Public for PortfolioDigViewModeler constructor
@Stable
class MutablePortfolioDigViewState
@Inject
internal constructor(
    params: PortfolioDigParams,
) : MutableDigViewState(params.symbol), PortfolioDigViewState {
  override val section = MutableStateFlow(PortfolioDigSections.POSITIONS)

  override val splitDialog = MutableStateFlow<SplitParams?>(null)
  override val stockSplitError = MutableStateFlow<Throwable?>(null)
  override val stockSplits: StateFlow<List<DbSplit>>

  override val holding = MutableStateFlow<DbHolding?>(null)
  override val holdingError = MutableStateFlow<Throwable?>(null)

  override val positionsError = MutableStateFlow<Throwable?>(null)
  override val positions: StateFlow<List<PositionStock>>

  private val realPositions = MutableStateFlow(emptyList<PositionStock>())
  private val realSplit = MutableStateFlow(emptyList<DbSplit>())

  init {
    positions = realPositions
    stockSplits = realSplit
  }

  /**
   * This function ensures along with private setters, that positions and splits are always updated
   * together to be consistent
   */
  internal fun handlePositionListRegenOnSplitsUpdated(
      positions: List<PositionStock> = this.positions.value,
      splits: List<DbSplit> = this.stockSplits.value,
  ) {
    val self = this
    self.realSplit.value = splits
    self.realPositions.value = positions.map { it.copy(splits = splits) }
  }
}
