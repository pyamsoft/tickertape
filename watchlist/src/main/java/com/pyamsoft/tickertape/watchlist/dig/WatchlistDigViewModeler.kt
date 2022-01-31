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
import com.pyamsoft.tickertape.quote.TickerInteractor
import com.pyamsoft.tickertape.quote.dig.DigViewModeler
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistDigViewModeler
@Inject
internal constructor(
    private val state: MutableWatchlistDigViewState,
    private val interactor: WatchlistDigInteractor,
) :
    DigViewModeler<MutableWatchlistDigViewState>(
        state,
        interactor,
    ) {

  private val loadRunner =
      highlander<Unit, Boolean> { force ->
        awaitAll(
            async { handleLoadTicker(force) },
            async { handleIsInWatchlist(force) },
        )
      }

  private val watchlistModifyRunner =
      highlander<ResultWrapper<Boolean>> {
        interactor.modifyWatchlist(
            symbol = state.ticker.symbol,
        )
      }

  private suspend fun handleIsInWatchlist(force: Boolean) {
    interactor
        .isInWatchlist(symbol = state.ticker.symbol, force)
        .onSuccess { s ->
          Timber.d("Symbol is in watchlist: $s")
          state.isInWatchlist = s
        }
        .onFailure { e ->
          Timber.e(e, "Error loading symbol in watchlist")
          // Don't need to clear the ticker since last loaded state was valid
          state.error = e
        }
  }

  private suspend fun handleLoadTicker(force: Boolean) {
    val s = state

    onLoadTicker(
        force = force,
    )
        .onSuccess {
          // Clear the error on load success
          s.error = null
        }
        .onFailure {
          // Don't need to clear the ticker since last loaded state was valid
          s.error = it
        }
  }

  override fun handleLoadTicker(scope: CoroutineScope, force: Boolean) {
    state.isLoading = true
    scope.launch(context = Dispatchers.Main) {
      try {
        loadRunner.call(force)
      } finally {
        state.isLoading = false
      }
    }
  }

  fun handleModifyWatchlist(scope: CoroutineScope) {
    val s = state
    if (!s.isAllowModifyWatchlist) {
      throw IllegalStateException("Add to watchlist handler called but not allowed here!")
    }

    scope.launch(context = Dispatchers.Main) {
      watchlistModifyRunner
          .call()
          .onFailure { Timber.e(it, "Error adding symbol to watchlist") }
          .onSuccess { s ->
            Timber.d("Add symbol to watchlist: $s")
            state.isInWatchlist = s
          }
          .onFailure {
            // TODO handle error
          }
    }
  }

  companion object {
    private val TICKER_OPTIONS_NO_BIG_MOVERS =
        TickerInteractor.Options(
            notifyBigMovers = false,
        )

    private val TICKER_OPTIONS_BIG_MOVERS =
        TickerInteractor.Options(
            notifyBigMovers = true,
        )
  }
}
