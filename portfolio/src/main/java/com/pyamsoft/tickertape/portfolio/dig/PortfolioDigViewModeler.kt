/*
 * Copyright 2023 pyamsoft
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
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.db.Maybe
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.db.position.PositionChangeEvent
import com.pyamsoft.tickertape.db.split.DbSplit
import com.pyamsoft.tickertape.db.split.SplitChangeEvent
import com.pyamsoft.tickertape.portfolio.dig.position.PositionStock
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.add.NewTickerInteractor
import com.pyamsoft.tickertape.quote.chart.ChartDataProcessor
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import com.pyamsoft.tickertape.quote.dig.PortfolioDigParams
import com.pyamsoft.tickertape.quote.dig.PositionParams
import com.pyamsoft.tickertape.quote.dig.SplitParams
import com.pyamsoft.tickertape.stocks.JsonParser
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class PortfolioDigViewModeler
@Inject
internal constructor(
    override val state: MutablePortfolioDigViewState,
    private val params: PortfolioDigParams,
    private val newInteractor: NewTickerInteractor,
    private val interactor: PortfolioDigInteractor,
    private val jsonParser: JsonParser,
    private val interactorCache: PortfolioDigInteractor.Cache,
    processor: ChartDataProcessor,
) :
    PortfolioDigViewState by state,
    DigViewModeler<MutablePortfolioDigViewState>(
        state,
        params.lookupSymbol,
        processor,
        interactor,
        interactorCache,
    ) {

  private var positionLoadJob: Deferred<ResultWrapper<List<PositionStock>>>? = null
  private var splitLoadJob: Deferred<ResultWrapper<List<DbSplit>>>? = null

  @CheckResult
  private suspend fun handleLoadSplits(force: Boolean): ResultWrapper<List<DbSplit>> =
      when (val holding = state.holding.value) {
        is Maybe.Data -> {
          val data = holding.data
          if (force) {
            interactorCache.invalidateSplits(data.id)
          }

          interactor.getSplits(data.id)
        }
        null,
        is Maybe.None -> {
          ResultWrapper.success(emptyList())
        }
      }

  @CheckResult
  private suspend fun handleLoadHolding(force: Boolean): ResultWrapper<Maybe<out DbHolding>> {

    // If this holding is already provided, great, fast track!
    val holding = params.holding
    return if (holding != null) {
      ResultWrapper.success(Maybe.Data(holding))
    } else {
      if (force) {
        interactorCache.invalidateHolding(params.symbol)
      }

      interactor.getHolding(params.symbol)
    }
  }

  @CheckResult
  private suspend fun handleLoadPositions(
      force: Boolean,
      splits: List<DbSplit>
  ): ResultWrapper<List<PositionStock>> =
      when (val holding = state.holding.value) {
        is Maybe.Data -> {
          val data = holding.data
          if (force) {
            interactorCache.invalidatePositions(data.id)
          }

          interactor.getPositions(data.id).map { p ->
            p.map { createPositionStock(data, it, splits) }.sortedBy { it.purchaseDate }
          }
        }
        null,
        is Maybe.None -> ResultWrapper.success(emptyList())
      }

  private suspend fun handleLoadAll(force: Boolean) = coroutineScope {
    // Load the holding first, always
    loadHolding(force)

    mutableListOf<Deferred<*>>()
        .apply {

          // Always load the ticker in parallel
          add(loadTickerAsync(force))

          when (state.section.value) {
            PortfolioDigSections.PRICE_ALERTS -> {
              // TODO add price alerts work
            }
            PortfolioDigSections.CHART -> {
              // Chart doesn't need anything specific
            }
            PortfolioDigSections.NEWS -> {
              add(loadNewsAsync(force))
            }
            PortfolioDigSections.STATISTICS -> {
              add(loadStatisticsAsync(force))
            }
            PortfolioDigSections.SPLITS -> {
              add(loadSplitsAsync(force))
            }
            PortfolioDigSections.POSITIONS -> {
              add(loadSplitsAsync(force))
              add(loadPositionsAsync(force))
            }
            PortfolioDigSections.RECOMMENDATIONS -> {
              add(loadRecommendationsAsync(force))
            }
            PortfolioDigSections.OPTIONS_CHAIN -> {
              add(loadOptionsChainAsync(force))
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

  @CheckResult
  private suspend fun CoroutineScope.loadSplitsAsync(
      force: Boolean
  ): Deferred<ResultWrapper<List<DbSplit>>> {
    val scope = this
    val s = state

    splitLoadJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          handleLoadSplits(force)
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
        .also { splitLoadJob = it }
  }

  private suspend fun loadHolding(force: Boolean) {
    handleLoadHolding(force)
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

  @CheckResult
  private suspend fun CoroutineScope.loadPositionsAsync(
      force: Boolean
  ): Deferred<ResultWrapper<List<PositionStock>>> {
    val scope = this

    val s = state
    val splits = s.stockSplits.value

    positionLoadJob?.cancel()
    return scope
        .async(context = Dispatchers.Default) {
          handleLoadPositions(force, splits)
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
        .also { positionLoadJob = it }
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
    when (val holding = state.holding.value) {
      is Maybe.Data -> {
        onPositionInsertOrUpdate(position, holding.data)
      }
      null,
      is Maybe.None -> {
        Timber.w("Drop position update, missing holding")
      }
    }
  }

  private fun onPositionInserted(position: DbPosition) {
    when (val holding = state.holding.value) {
      is Maybe.Data -> {
        onPositionInsertOrUpdate(position, holding.data)
      }
      null,
      is Maybe.None -> {
        Timber.w("Drop position insert, missing holding")
      }
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

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == BaseDigViewState.LoadingState.LOADING) {
      return
    }

    scope.launch(context = Dispatchers.Default) {
      if (state.loadingState.value == BaseDigViewState.LoadingState.LOADING) {
        return@launch
      }

      Timber.d("Start loading everything")
      state.loadingState.value = BaseDigViewState.LoadingState.LOADING

      handleLoadAll(force)

      Timber.d("Done loading everything")
      state.loadingState.value = BaseDigViewState.LoadingState.DONE
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

  override fun onDispose() {
    positionLoadJob?.cancel()
    positionLoadJob = null

    splitLoadJob?.cancel()
    splitLoadJob = null
  }

  fun bind(scope: CoroutineScope) {
    interactor.watchPositions().also { f ->
      scope.launch(context = Dispatchers.Default) { f.collect { onPositionChangeEvent(it) } }
    }

    interactor.watchSplits().also { f ->
      scope.launch(context = Dispatchers.Default) { f.collect { onSplitChangeEvent(it) } }
    }
  }

  fun handleDeleteSplit(
      scope: CoroutineScope,
      split: DbSplit,
  ) {
    scope.launch(context = Dispatchers.Default) {
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
    scope.launch(context = Dispatchers.Default) {
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
    val quote = ticker.quote ?: return

    var lookupSymbol: StockSymbol? = null
    if (quote.type == EquityType.OPTION) {
      if (quote is StockOptionsQuote) {
        lookupSymbol = quote.underlyingSymbol
      }
    }

    handleOpenRec(
        PortfolioDigParams(
            symbol = ticker.symbol,
            equityType = quote.type,
            lookupSymbol = lookupSymbol,
            currentPrice = quote.currentSession.price,
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

  fun handleAddTicker(scope: CoroutineScope) {
    val h = state.holding.value ?: return

    scope.launch(context = Dispatchers.Default) {
      when (h) {
        is Maybe.Data -> {
          Timber.w("Cannot delete existing holding: ${h.data.id} ${h.data.symbol}")
        }
        is Maybe.None -> {
          newInteractor
              .insertNewTicker(
                  symbol = params.symbol,
                  equityType = params.equityType,
                  tradeSide = TradeSide.BUY,
              )
              .onSuccess {
                // Refresh screen
                handleLoadTicker(
                    scope = this,
                    force = true,
                )
              }
              .onFailure { Timber.e(it, "Failed adding new quote: ${params.symbol}") }
        }
      }
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
