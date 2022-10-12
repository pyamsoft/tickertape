/*
 * Copyright 2021 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import androidx.annotation.CheckResult
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.portfolio.dig.position.PositionStock
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

class PortfolioDigViewModeler
@Inject
internal constructor(
    private val state: MutablePortfolioDigViewState,
    private val equityType: EquityType,
    private val tradeSide: TradeSide,
    private val currentPrice: StockMoneyValue?,
    private val interactor: PortfolioDigInteractor,
    @Named("lookup") lookupSymbol: StockSymbol?,
    interactorCache: PortfolioDigInteractor.Cache,
    holdingId: DbHolding.Id,
) :
    DigViewModeler<MutablePortfolioDigViewState>(
        state,
        lookupSymbol,
        interactor,
        interactorCache,
    ) {

  private val splitLoadRunner =
      highlander<ResultWrapper<List<DbSplit>>, Boolean> { force ->
        if (force) {
          interactorCache.invalidateSplits()
        }
        return@highlander interactor.getSplits(holdingId)
      }

  private val holdingLoadRunner =
      highlander<ResultWrapper<DbHolding>, Boolean> { force ->
        if (force) {
          interactorCache.invalidateHolding()
        }
        return@highlander interactor.getHolding(holdingId)
      }

  private val positionsLoadRunner =
      highlander<ResultWrapper<List<PositionStock>>, Boolean, List<DbSplit>> { force, splits ->
        if (force) {
          interactorCache.invalidatePositions()
        }

        return@highlander interactor.getPositions(holdingId).map { p ->
          p.map { createPositionStock(it, splits) }.sortedBy { it.purchaseDate }
        }
      }

  private val positionDeleteRunner =
      highlander<ResultWrapper<Boolean>, DbPosition> { interactor.deletePosition(it) }

  private val splitDeleteRunner =
      highlander<ResultWrapper<Boolean>, DbSplit> { interactor.deleteSplit(it) }

  private val loadRunner =
      highlander<Unit, Boolean> { force ->
        mutableListOf<Deferred<*>>()
            .apply {
              @Suppress("ControlFlowWithEmptyBody")
              when (state.section) {
                PortfolioDigSections.CHART -> {
                  add(async { loadTicker(force) })
                }
                PortfolioDigSections.NEWS -> {
                  add(async { loadNews(force) })
                }
                PortfolioDigSections.STATISTICS -> {
                  add(async { loadStatistics(force) })
                }
                PortfolioDigSections.SPLITS -> {
                  add(async { loadSplits(force) })
                }
                PortfolioDigSections.POSITIONS -> {
                  add(async { loadHolding(force) })
                  add(async { loadSplits(force) })
                  add(async { loadPositions(force) })
                }
                PortfolioDigSections.RECOMMENDATIONS -> {
                  add(async { loadRecommendations(force) })
                }
                PortfolioDigSections.OPTIONS_CHAIN -> {
                  add(async { loadOptionsChain(force) })
                }
              }.also {
                // Just here for exhaustive when
              }
            }
            .awaitAll()
      }

  @CheckResult
  private fun createPositionStock(
      position: DbPosition,
      splits: List<DbSplit>,
  ): PositionStock {
    return PositionStock(
        position = position,
        equityType = equityType,
        tradeSide = tradeSide,
        currentPrice = currentPrice,
        splits = splits,
    )
  }

  private suspend fun loadSplits(force: Boolean) {
    val s = state
    splitLoadRunner
        .call(force)
        .onSuccess { sp ->
          s.apply {
            stockSplitError = null
            s.handlePositionListRegenOnSplitsUpdated(splits = sp)
          }
        }
        .onFailure { e ->
          s.apply {
            stockSplitError = e
            s.handlePositionListRegenOnSplitsUpdated(splits = emptyList())
          }
        }
  }

  private suspend fun loadHolding(force: Boolean) {
    holdingLoadRunner
        .call(force)
        .onSuccess { h ->
          state.apply {
            holding = h
            holdingError = null
          }
        }
        .onFailure { e ->
          state.apply {
            holding = null
            holdingError = e
          }
        }
  }

  private suspend fun loadPositions(force: Boolean) {
    val s = state
    val splits = s.stockSplits

    positionsLoadRunner
        .call(force, splits)
        .onSuccess { p ->
          s.apply {
            positionsError = null
            s.handlePositionListRegenOnSplitsUpdated(positions = p)
          }
        }
        .onFailure { e ->
          s.apply {
            positionsError = e
            s.handlePositionListRegenOnSplitsUpdated(positions = emptyList())
          }
        }
  }

  private fun onPositionChangeEvent(event: PositionChangeEvent) {
    return when (event) {
      is PositionChangeEvent.Delete -> onPostionDeleted(event.position, event.offerUndo)
      is PositionChangeEvent.Insert -> onPositionInserted(event.position)
      is PositionChangeEvent.Update -> onPositionUpdated(event.position)
    }
  }

  private fun onPositionInsertOrUpdate(position: DbPosition) {
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(
        positions =
            insertOrUpdate(
                s.positions,
                createPositionStock(
                    position,
                    s.stockSplits,
                ),
            ) {
              it.holdingId == position.holdingId && it.id == position.id
            },
    )
  }

  private fun onPositionUpdated(position: DbPosition) {
    onPositionInsertOrUpdate(position)
  }

  private fun onPositionInserted(position: DbPosition) {
    onPositionInsertOrUpdate(position)
  }

  private fun onPostionDeleted(position: DbPosition, offerUndo: Boolean) {
    // TODO handle offerUndo?
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(
        positions = s.positions.filterNot { it.id == position.id })
  }

  private fun onSplitChangeEvent(event: SplitChangeEvent) {
    return when (event) {
      is SplitChangeEvent.Delete -> onSplitDeleted(event.split, event.offerUndo)
      is SplitChangeEvent.Insert -> onSplitInserted(event.split)
      is SplitChangeEvent.Update -> onSplitUpdated(event.split)
    }
  }

  private fun onSplitInsertOrUpdate(split: DbSplit) {
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(
        splits =
            insertOrUpdate(s.stockSplits, split) {
              it.holdingId == split.holdingId && it.id == split.id
            })
  }

  private fun onSplitUpdated(split: DbSplit) {
    onSplitInsertOrUpdate(split)
  }

  private fun onSplitInserted(split: DbSplit) {
    onSplitInsertOrUpdate(split)
  }

  private fun onSplitDeleted(split: DbSplit, offerUndo: Boolean) {
    // TODO handle offerUndo?
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(splits = s.stockSplits.filterNot { it.id == split.id })
  }

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      loadRunner.call(force).also { state.isLoading = false }
    }
  }

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.watchPositions { onPositionChangeEvent(it) }
    }

    scope.launch(context = Dispatchers.Main) { interactor.watchSplits { onSplitChangeEvent(it) } }
  }

  fun handleDeleteSplit(
      scope: CoroutineScope,
      split: DbSplit,
  ) {
    scope.launch(context = Dispatchers.Main) {
      splitDeleteRunner
          .call(split)
          .onFailure { Timber.e(it, "Failed to delete split: $split") }
          .onSuccess { deleted ->
            if (deleted) {
              Timber.d("Position split: $split")
            } else {
              Timber.w("Position was not split: $split")
            }
          }
    }
  }

  fun handleDeletePosition(
      scope: CoroutineScope,
      position: DbPosition,
  ) {
    scope.launch(context = Dispatchers.Main) {
      positionDeleteRunner
          .call(position)
          .onFailure { Timber.e(it, "Failed to delete position: $position") }
          .onSuccess { deleted ->
            if (deleted) {
              Timber.d("Position deleted: $position")
            } else {
              Timber.w("Position was not deleted: $position")
            }
          }
    }
  }

  fun handleTabUpdated(scope: CoroutineScope, section: PortfolioDigSections) {
    state.section = section
    handleLoadTicker(scope, force = false)
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun <T : Any> insertOrUpdate(
        list: List<T>,
        item: T,
        isMatching: (item: T) -> Boolean
    ): List<T> {
      return list.toMutableList().apply {
        val index = this.indexOfFirst { isMatching(it) }

        if (index < 0) {
          // No index, this is a new item
          add(item)
        } else {
          // Update existing item at index
          set(index, item)
        }
      }
    }
  }
}
