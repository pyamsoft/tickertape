package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.position.PositionStock
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

@Stable
interface PortfolioDigViewState : DigViewState {
  val isLoading: Boolean
  val section: PortfolioDigSections

  val stockSplits: List<DbSplit>
  val stockSplitError: Throwable?

  val holding: DbHolding?
  val holdingError: Throwable?

  val positions: List<PositionStock>
  val positionsError: Throwable?
}

// Public for PortfolioDigViewModeler constructor
@Stable
class MutablePortfolioDigViewState
@Inject
internal constructor(
    symbol: StockSymbol,
) : MutableDigViewState(symbol), PortfolioDigViewState {
  override var isLoading by mutableStateOf(false)
  override var section by mutableStateOf(PortfolioDigSections.POSITIONS)

  override var stockSplitError by mutableStateOf<Throwable?>(null)
  override var stockSplits by mutableStateOf(emptyList<DbSplit>())
    private set

  override var holding by mutableStateOf<DbHolding?>(null)
  override var holdingError by mutableStateOf<Throwable?>(null)

  override var positionsError by mutableStateOf<Throwable?>(null)
  override var positions by mutableStateOf(emptyList<PositionStock>())
    private set

  /**
   * This function ensures along with private setters, that positions and splits are always updated
   * together to be consistent
   */
  internal fun handlePositionListRegenOnSplitsUpdated(
      positions: List<PositionStock> = this.positions,
      splits: List<DbSplit> = this.stockSplits,
  ) {
    val self = this
    self.stockSplits = splits
    self.positions = positions.map { it.copy(splits = splits) }
  }
}
