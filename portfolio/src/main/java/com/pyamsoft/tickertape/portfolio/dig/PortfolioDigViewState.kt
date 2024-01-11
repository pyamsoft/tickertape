/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.portfolio.dig

import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.portfolio.dig.position.PositionStock
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.DigViewState
import com.pyamsoft.tickertape.quote.dig.MutableDigViewState
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.quote.dig.SplitParams
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface BasePortfolioDigViewState : BaseDigViewState {
  val section: StateFlow<PortfolioDigSections>

  val holding: StateFlow<Maybe<out DbHolding>?>
  val holdingError: StateFlow<Throwable?>
}

@Stable
interface PositionsPortfolioDigViewState : BasePortfolioDigViewState {
  val recentlyDeletePosition: StateFlow<DbPosition?>

  val positions: StateFlow<List<PositionStock>>
  val positionsError: StateFlow<Throwable?>
}

@Stable
interface SplitsPortfolioDigViewState : BasePortfolioDigViewState {
  val recentlyDeleteSplit: StateFlow<DbSplit?>

  val stockSplits: StateFlow<List<DbSplit>>
  val stockSplitError: StateFlow<Throwable?>
}

@Stable
interface PortfolioDigViewState :
    DigViewState, PositionsPortfolioDigViewState, SplitsPortfolioDigViewState {
  val splitDialog: StateFlow<SplitParams?>
  val positionDialog: StateFlow<PositionParams?>
  val recommendedDig: StateFlow<PortfolioDigParams?>
}

// Public for PortfolioDigViewModeler constructor
@Stable
class MutablePortfolioDigViewState
@Inject
internal constructor(
    params: PortfolioDigParams,
    clock: Clock,
) : MutableDigViewState(params.symbol, clock), PortfolioDigViewState {
  override val section = MutableStateFlow(PortfolioDigSections.POSITIONS)

  override val recentlyDeleteSplit = MutableStateFlow<DbSplit?>(null)
  override val splitDialog = MutableStateFlow<SplitParams?>(null)
  override val stockSplitError = MutableStateFlow<Throwable?>(null)
  override val stockSplits: StateFlow<List<DbSplit>>

  override val holding = MutableStateFlow<Maybe<out DbHolding>?>(null)
  override val holdingError = MutableStateFlow<Throwable?>(null)

  override val positionDialog = MutableStateFlow<PositionParams?>(null)
  override val positionsError = MutableStateFlow<Throwable?>(null)
  override val recentlyDeletePosition = MutableStateFlow<DbPosition?>(null)
  override val positions: StateFlow<List<PositionStock>>

  private val realPositions = MutableStateFlow(emptyList<PositionStock>())
  private val realSplit = MutableStateFlow(emptyList<DbSplit>())

  override val recommendedDig = MutableStateFlow<PortfolioDigParams?>(null)

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
