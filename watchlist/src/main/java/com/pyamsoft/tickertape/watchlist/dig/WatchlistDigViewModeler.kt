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

package com.pyamsoft.tickertape.watchlist.dig

import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.tickertape.quote.dig.BaseDigViewState
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistDigViewModeler
@Inject
internal constructor(
    override val state: MutableWatchlistDigViewState,
    private val interactor: WatchlistDigInteractor,
    private val interactorCache: WatchlistDigInteractor.Cache,
    @Named("lookup") lookupSymbol: StockSymbol?,
) :
    DigViewModeler<MutableWatchlistDigViewState>(
        state,
        lookupSymbol,
        interactor,
        interactorCache,
    ) {

  private val loadRunner =
      highlander<Unit, Boolean> { force ->
        mutableListOf<Deferred<*>>()
            .apply {
              // Always this
              add(async { checkIsInWatchlist(force) })

              // Always load ticker
              add(async { loadTicker(force) })

              // Based on the page
              @Suppress("ControlFlowWithEmptyBody", "IMPLICIT_CAST_TO_ANY")
              when (state.section.value) {
                WatchlistDigSections.PRICE_ALERTS -> {
                  // TODO add price alerts work
                }
                WatchlistDigSections.CHART -> {
                  // Chart doesn't need anything specific
                }
                WatchlistDigSections.NEWS -> {
                  add(async { loadNews(force) })
                }
                WatchlistDigSections.STATISTICS -> {
                  add(async { loadStatistics(force) })
                }
                WatchlistDigSections.RECOMMENDATIONS -> {
                  add(async { loadRecommendations(force) })
                }
                WatchlistDigSections.OPTIONS_CHAIN -> {
                  add(async { loadOptionsChain(force) })
                }
              }.also {
                // Just here for exhaustive when
              }
            }
            .awaitAll()
      }

  private val watchlistModifyRunner =
      highlander<ResultWrapper<Boolean>> {
        interactor.modifyWatchlist(
            symbol = state.ticker.value.symbol,
        )
      }

  private suspend fun checkIsInWatchlist(force: Boolean) {
    val s = state
    if (force) {
      interactorCache.invalidateIsInWatchlist()
    }

    interactor
        .isInWatchlist(state.ticker.value.symbol)
        .onSuccess { isIn ->
          Timber.d("Symbol is in watchlist: $isIn")
          s.apply {
            watchlistStatus.value =
                if (isIn) WatchlistDigViewState.WatchlistStatus.IN_LIST
                else WatchlistDigViewState.WatchlistStatus.NOT_IN_LIST
            isInWatchlistError.value = null
          }
        }
        .onFailure { e ->
          Timber.e(e, "Error loading symbol in watchlist")
          // Don't need to clear the ticker since last loaded state was valid
          s.apply {
            watchlistStatus.value = WatchlistDigViewState.WatchlistStatus.NONE
            isInWatchlistError.value = e
          }
        }
  }

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == BaseDigViewState.LoadingState.LOADING) {
      return
    }

    state.loadingState.value = BaseDigViewState.LoadingState.LOADING
    scope.launch(context = Dispatchers.Main) {
      try {
        loadRunner.call(force)
      } finally {
        state.loadingState.value = BaseDigViewState.LoadingState.DONE
      }
    }
  }

  fun handleModifyWatchlist(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      watchlistModifyRunner
          .call()
          .onFailure { Timber.e(it, "Error modifying watchlist") }
          .onSuccess { s ->
            Timber.d("Modify watchlist: $s")
            state.watchlistStatus.value =
                if (s) WatchlistDigViewState.WatchlistStatus.IN_LIST
                else WatchlistDigViewState.WatchlistStatus.NOT_IN_LIST
          }
          .onFailure {
            // TODO handle error
            Timber.e(it, "Failed to modify watchlist")
          }
    }
  }

  fun handleTabUpdated(
      scope: CoroutineScope,
      section: WatchlistDigSections,
  ) {
    state.section.value = section
    handleLoadTicker(scope, force = false)
  }
}
