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
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import com.pyamsoft.tickertape.stocks.api.StockNews
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

class PortfolioDigViewModeler
@Inject
internal constructor(
    private val state: MutablePortfolioDigViewState,
    private val holdingId: DbHolding.Id,
    private val interactor: PortfolioDigInteractor,
) :
    DigViewModeler<MutablePortfolioDigViewState>(
        state,
        interactor,
    ) {

  private val deleteRunner =
      highlander<ResultWrapper<Boolean>, DbPosition> { interactor.deletePositon(it) }

  private val loadRunner =
      highlander<Unit, Boolean> { force ->
        awaitAll(
            async { loadTicker(force) },
            async { loadNews(force) },
            async { loadHolding(force) },
            async { loadPositions(force) },
        )
      }

  @CheckResult
  private suspend fun loadNews(force: Boolean): ResultWrapper<List<StockNews>> {
    return interactor
        .getNews(force, symbol = state.ticker.symbol)
        .onSuccess { n ->
          // Clear the error on load success
          state.apply {
            news = n
            newsError = null
          }
        }
        .onFailure { e ->
          // Don't need to clear the ticker since last loaded state was valid
          state.apply {
            news = emptyList()
            newsError = e
          }
        }
  }

  @CheckResult
  private suspend fun loadTicker(force: Boolean): ResultWrapper<Ticker> {
    return onLoadTicker(
        force,
    )
        .onSuccess {
          // Clear the error on load success
          state.chartError = null
        }
        .onFailure {
          // Don't need to clear the ticker since last loaded state was valid
          state.chartError = it
        }
  }

  @CheckResult
  private suspend fun loadHolding(force: Boolean): ResultWrapper<DbHolding> {
    return interactor
        .getHolding(force, holdingId)
        .onSuccess { h ->
          // Clear the error on load success
          state.apply {
            holding = h
            holdingError = null
          }
        }
        .onFailure { e ->
          // Clear holding on load fail
          state.apply {
            holding = null
            holdingError = e
          }
        }
  }

  @CheckResult
  private suspend fun loadPositions(force: Boolean): ResultWrapper<List<DbPosition>> {
    return interactor
        .getPositions(force, holdingId)
        .onSuccess { p ->
          // Clear the error on load success
          state.apply {
            positions = p
            positionsError = null
          }
        }
        .onFailure { e ->
          // Clear positions on load fail
          state.apply {
            positions = emptyList()
            positionsError = e
          }
        }
  }

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      loadRunner.call(force).also { state.isLoading = false }
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
    val newPositions =
        s.positions.toMutableList().apply {
          val index =
              this.indexOfFirst {
                it.holdingId() == position.holdingId() && it.id() == position.id()
              }

          if (index < 0) {
            // No index, this is a new item
            add(position)
          } else {
            // Update existing item at index
            set(index, position)
          }
        }
    s.positions = newPositions
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
    s.positions = s.positions.filterNot { it.id() == position.id() }
  }

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.watchPositions { onPositionChangeEvent(it) }
    }
  }

  fun handleDeletePosition(
      scope: CoroutineScope,
      position: DbPosition,
  ) {
    scope.launch(context = Dispatchers.Main) {
      deleteRunner
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

  fun handleTabUpdated(section: PortfolioDigSections) {
    state.section = section
  }
}
