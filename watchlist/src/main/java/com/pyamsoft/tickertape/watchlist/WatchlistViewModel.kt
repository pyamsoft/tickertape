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

package com.pyamsoft.tickertape.watchlist

import androidx.lifecycle.viewModelScope
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.onActualError
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WatchlistViewModel @Inject internal constructor(private val interactor: WatchlistInteractor) :
    UiViewModel<WatchListViewState, WatchListControllerEvent>(
        initialState = WatchListViewState(error = null, isLoading = false, quotes = emptyList())
    ) {

    private val quoteFetcher =
        highlander<Unit, Boolean> { force ->
            setState(
                stateChange = { copy(isLoading = true) },
                andThen = {
                    try {
                        val quotes = interactor.getQuotes(force)
                        setState { copy(error = null, quotes = quotes, isLoading = false) }
                    } catch (error: Throwable) {
                        error.onActualError { e ->
                            Timber.e(e, "Failed to fetch quotes")
                            setState { copy(error = e, isLoading = false) }
                        }
                    }
                })
        }

    fun fetchQuotes(force: Boolean) {
        viewModelScope.launch(context = Dispatchers.Default) {
            quoteFetcher.call(force)
        }
    }
}
