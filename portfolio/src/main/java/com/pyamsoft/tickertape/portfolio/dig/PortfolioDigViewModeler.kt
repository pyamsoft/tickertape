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
import com.pyamsoft.tickertape.quote.Ticker
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

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

  private val loadRunner =
      highlander<Unit, Boolean> { force ->
        awaitAll(
            async { loadTicker(force) },
            async { loadHolding(force) },
            async { loadPositions(force) },
        )
      }

  @CheckResult
  private suspend fun loadTicker(force: Boolean): ResultWrapper<Ticker> {
    return onLoadTicker(force)
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

  fun handleTabUpdated(section: PortfolioDigSections) {
    state.section = section
  }
}
