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
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.portfolio.dig.position.PositionStock
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.quote.dig.SplitParams
import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PortfolioDigViewModeler
@Inject
internal constructor(
    override val state: MutablePortfolioDigViewState,
    private val params: PortfolioDigParams,
    private val interactor: PortfolioDigInteractor,
    private val jsonParser: JsonParser,
    interactorCache: PortfolioDigInteractor.Cache,
) :
    DigViewModeler<MutablePortfolioDigViewState>(
        state,
        params.lookupSymbol,
        interactor,
        interactorCache,
    ) {

  private val splitLoadRunner =
      highlander<ResultWrapper<List<DbSplit>>, Boolean> { force ->
        if (force) {
          interactorCache.invalidateSplits()
        }

        val holding = state.holding.value
        return@highlander if (holding == null) {
          ResultWrapper.success(emptyList())
        } else {
          interactor.getSplits(holding.id)
        }
      }

  private val holdingLoadRunner =
      highlander<ResultWrapper<DbHolding>, Boolean> { force ->
        if (force) {
          interactorCache.invalidateHolding()
        }

        // If thie holding is already provided, great, fast track!
        val holding = params.holding
        return@highlander if (holding != null) {
          ResultWrapper.success(holding)
        } else {
          interactor.getHolding(params.symbol)
        }
      }

  private val positionsLoadRunner =
      highlander<ResultWrapper<List<PositionStock>>, Boolean, List<DbSplit>> { force, splits ->
        if (force) {
          interactorCache.invalidatePositions()
        }

        val holding = state.holding.value
        return@highlander if (holding == null) {
          ResultWrapper.success(emptyList())
        } else {
          interactor.getPositions(holding.id).map { p ->
            p.map { createPositionStock(holding, it, splits) }.sortedBy { it.purchaseDate }
          }
        }
      }

  @Suppress("ControlFlowWithEmptyBody")
  private val loadRunner =
      highlander<Unit, Boolean> { force ->

        // Load the holding first, always
        loadHolding(force)

        mutableListOf<Deferred<*>>()
            .apply {

              // Always load the ticker in parallel
              add(async { loadTicker(force) })

              @Suppress("ControlFlowWithEmptyBody", "IMPLICIT_CAST_TO_ANY")
              when (state.section.value) {
                PortfolioDigSections.PRICE_ALERTS -> {
                  // TODO add price alerts work
                }
                PortfolioDigSections.CHART -> {
                  // Chart doesn't need anything specific
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
                  add(async { loadSplits(force) })
                  add(async { loadPositions(force) })
                }
                PortfolioDigSections.RECOMMENDATIONS -> {
                  add(async { loadRecommendations(force) })
                }
                PortfolioDigSections.OPTIONS_CHAIN -> {
                  add(async { loadOptionsChain(force) })
                }
              }
            }
            .awaitAll()
      }

  @CheckResult
  private fun createPositionStock(
      holding: DbHolding,
      position: DbPosition,
      splits: List<DbSplit>,
  ): PositionStock {
    return PositionStock(
        position = position,
        equityType = holding.type,
        tradeSide = holding.side,
        currentPrice = params.currentPrice,
        splits = splits,
    )
  }

  private suspend fun loadSplits(force: Boolean) {
    val s = state
    splitLoadRunner
        .call(force)
        .onSuccess { sp ->
          s.apply {
            stockSplitError.value = null
            s.handlePositionListRegenOnSplitsUpdated(splits = sp)
          }
        }
        .onFailure { e ->
          s.apply {
            stockSplitError.value = e
            s.handlePositionListRegenOnSplitsUpdated(splits = emptyList())
          }
        }
  }

  private suspend fun loadHolding(force: Boolean) {
    holdingLoadRunner
        .call(force)
        .onSuccess { h ->
          state.apply {
            holding.value = h
            holdingError.value = null
          }
        }
        .onFailure { e ->
          state.apply {
            holding.value = null
            holdingError.value = e
          }
        }
  }

  private suspend fun loadPositions(force: Boolean) {
    val s = state
    val splits = s.stockSplits.value

    positionsLoadRunner
        .call(force, splits)
        .onSuccess { p ->
          s.apply {
            positionsError.value = null
            s.handlePositionListRegenOnSplitsUpdated(positions = p)
          }
        }
        .onFailure { e ->
          s.apply {
            positionsError.value = e
            s.handlePositionListRegenOnSplitsUpdated(positions = emptyList())
          }
        }
  }

  private fun onPositionChangeEvent(event: PositionChangeEvent) {
    return when (event) {
      is PositionChangeEvent.Delete -> onPositionDeleted(event.position, event.offerUndo)
      is PositionChangeEvent.Insert -> onPositionInserted(event.position)
      is PositionChangeEvent.Update -> onPositionUpdated(event.position)
    }
  }

  private fun onPositionInsertOrUpdate(position: DbPosition, holding: DbHolding) {
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(
        positions =
            insertOrUpdate(
                s.positions.value,
                createPositionStock(
                    holding,
                    position,
                    s.stockSplits.value,
                ),
            ) {
              it.holdingId == position.holdingId && it.id == position.id
            },
    )
  }

  private fun onPositionUpdated(position: DbPosition) {
    val holding = state.holding.value
    if (holding == null) {
      Timber.w("Drop position update, missing holding")
    } else {
      onPositionInsertOrUpdate(position, holding)
    }
  }

  private fun onPositionInserted(position: DbPosition) {
    val holding = state.holding.value
    if (holding == null) {
      Timber.w("Drop position insert, missing holding")
    } else {
      onPositionInsertOrUpdate(position, holding)
    }
  }

  private fun onPositionDeleted(position: DbPosition, offerUndo: Boolean) {
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(
        positions = s.positions.value.filterNot { it.id == position.id },
    )

    if (offerUndo) {
      Timber.d("Offer undo on position delete: $position")
      s.recentlyDeletePosition.value = position
    }
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
            insertOrUpdate(s.stockSplits.value, split) {
              it.holdingId == split.holdingId && it.id == split.id
            },
    )
  }

  private fun onSplitUpdated(split: DbSplit) {
    onSplitInsertOrUpdate(split)
  }

  private fun onSplitInserted(split: DbSplit) {
    onSplitInsertOrUpdate(split)
  }

  private fun onSplitDeleted(split: DbSplit, offerUndo: Boolean) {
    val s = state
    s.handlePositionListRegenOnSplitsUpdated(
        splits = s.stockSplits.value.filterNot { it.id == split.id },
    )

    if (offerUndo) {
      Timber.d("Offer undo on split delete: $split")
      s.recentlyDeleteSplit.value = split
    }
  }

  private fun handleOpenSplit(params: SplitParams) {
    state.splitDialog.value = params
  }

  private fun handleOpenPosition(params: PositionParams) {
    state.positionDialog.value = params
  }

  private fun handleOpenRec(params: PortfolioDigParams) {
    state.recommendedDig.value = params
  }

  private fun <T : Any> handleDeleteFinal(
      recentlyDeleted: MutableStateFlow<T?>,
      onDeleted: (T) -> Unit
  ) {
    val deleted = recentlyDeleted.getAndUpdate { null }
    if (deleted != null) {
      onDeleted(deleted)
    }
  }

  private fun <T : Any> handleRestoreDeleted(
      scope: CoroutineScope,
      recentlyDeleted: MutableStateFlow<T?>,
      restore: suspend (T) -> ResultWrapper<DbInsert.InsertResult<T>>
  ) {
    val deleted = recentlyDeleted.getAndUpdate { null }
    if (deleted != null) {
      scope.launch(context = Dispatchers.Main) {
        restore(deleted)
            .onFailure { Timber.e(it, "Error when restoring $deleted") }
            .onSuccess { result ->
              when (result) {
                is DbInsert.InsertResult.Insert -> Timber.d("Restored: ${result.data}")
                is DbInsert.InsertResult.Update -> Timber.d("Updated: ${result.data} from $deleted")
                is DbInsert.InsertResult.Fail -> {
                  Timber.e(result.error, "Failed to restore: $deleted")
                  // Caught by the onFailure below
                  throw result.error
                }
              }
            }
            .onFailure {
              Timber.e(it, "Failed to restore")
              // TODO handle restore error
            }
      }
    }
  }

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == BaseDigViewState.LoadingState.LOADING) {
      return
    }

    state.loadingState.value = BaseDigViewState.LoadingState.LOADING
    scope.launch(context = Dispatchers.Main) {
      loadRunner.call(force).also { state.loadingState.value = BaseDigViewState.LoadingState.DONE }
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {

        // We need the extra composeHash here so that each new PDE instance can keep its own data
        // without overriding the others

        val s = state

        registry
            .registerProvider(KEY_SPLIT_DIALOG) {
              s.splitDialog.value?.let { jsonParser.toJson(it.toJson()) }
            }
            .also { add(it) }

        registry
            .registerProvider(KEY_POSITION_DIALOG) {
              s.positionDialog.value?.let { jsonParser.toJson(it.toJson()) }
            }
            .also { add(it) }

        registry
            .registerProvider(KEY_REC) {
              s.recommendedDig.value?.let { jsonParser.toJson(it.toJson()) }
            }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {

    // We need the extra composeHash here so that each new PDE instance can keep its own data
    // without overriding the others

    registry
        .consumeRestored(KEY_SPLIT_DIALOG)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<SplitParams.Json>(it) }
        ?.fromJson()
        ?.also { handleOpenSplit(it) }

    registry
        .consumeRestored(KEY_POSITION_DIALOG)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<PositionParams.Json>(it) }
        ?.fromJson()
        ?.also { handleOpenPosition(it) }

    registry
        .consumeRestored(KEY_REC)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<PortfolioDigParams.Json>(it) }
        ?.fromJson()
        ?.also { handleOpenRec(it) }
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
      interactor
          .deleteSplit(split)
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
      interactor
          .deletePosition(position)
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
    state.section.value = section
    handleLoadTicker(scope, force = false)
  }

  fun handleOpenSplit(
      params: PortfolioDigParams,
      holding: DbHolding,
      split: DbSplit? = null,
  ) {
    handleOpenSplit(
        SplitParams(
            symbol = params.symbol,
            holdingId = holding.id,
            existingSplitId = split?.id ?: DbSplit.Id.EMPTY,
        ),
    )
  }

  fun handleCloseSplit() {
    state.splitDialog.value = null
  }

  fun handleOpenPosition(
      params: PortfolioDigParams,
      holding: DbHolding,
      position: DbPosition? = null,
  ) {
    handleOpenPosition(
        PositionParams(
            symbol = params.symbol,
            holdingId = holding.id,
            holdingType = holding.type,
            existingPositionId = position?.id ?: DbPosition.Id.EMPTY,
        ),
    )
  }

  fun handleClosePosition() {
    state.positionDialog.value = null
  }

  fun handleRecClicked(ticker: Ticker) {
    val quote = ticker.quote
    var lookupSymbol: StockSymbol? = null
    if (quote != null) {
      if (quote.type == EquityType.OPTION) {
        if (quote is StockOptionsQuote) {
          lookupSymbol = quote.underlyingSymbol
        }
      }
    }

    handleOpenRec(
        PortfolioDigParams(
            symbol = ticker.symbol,
            lookupSymbol = lookupSymbol,
            currentPrice = quote?.currentSession?.price,
        ),
    )
  }

  fun handleCloseRec() {
    state.recommendedDig.value = null
  }

  fun handlePositionDeleteFinal() {
    handleDeleteFinal(state.recentlyDeletePosition) { onPositionDeleted(it, offerUndo = false) }
  }

  fun handleRestoreDeletedPosition(scope: CoroutineScope) {
    handleRestoreDeleted(
        scope = scope,
        recentlyDeleted = state.recentlyDeletePosition,
    ) {
      interactor.restorePosition(it)
    }
  }

  fun handleSplitDeleteFinal() {
    handleDeleteFinal(state.recentlyDeleteSplit) { onSplitDeleted(it, offerUndo = false) }
  }

  fun handleRestoreDeletedSplit(scope: CoroutineScope) {
    handleRestoreDeleted(
        scope = scope,
        recentlyDeleted = state.recentlyDeleteSplit,
    ) {
      interactor.restoreSplit(it)
    }
  }

  companion object {

    private const val KEY_SPLIT_DIALOG = "key_split_dialog"
    private const val KEY_POSITION_DIALOG = "key_position_dialog"
    private const val KEY_REC = "key_rec"

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
