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

package com.pyamsoft.tickertape.quote.ui.chart

import androidx.lifecycle.ViewModelStore
import androidx.savedstate.SavedStateRegistryOwner
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.arch.newUiController
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.tickertape.quote.ui.QuoteViewState
import com.pyamsoft.tickertape.quote.ui.QuoteBaseController
import com.pyamsoft.tickertape.quote.ui.QuoteDelegateView
import javax.inject.Inject

internal class QuoteChartController @Inject internal constructor(
    factory: QuoteChartViewModel.Factory,
    store: ViewModelStore,
    private val owner: SavedStateRegistryOwner,
    private val chartView: QuoteChartView,
) : QuoteBaseController(), QuoteDelegateView by chartView {

    private val viewModel by fromViewModelFactory<QuoteChartViewModel>(store) {
        owner.createSavedStateViewModelFactory(factory)
    }

    private var stateSaver: StateSaver? = null

    override fun onCreate(savedInstanceState: UiSavedStateReader) {
        super.onCreate(savedInstanceState)
        stateSaver = createComponent(
            savedInstanceState.toBundle(),
            owner,
            viewModel,
            controller = newUiController {

        },
            chartView) {
            return@createComponent when (it) {
                is QuoteChartViewEvent.RangeUpdated -> viewModel.handleRangeUpdated(it.range)
                is QuoteChartViewEvent.Scrub -> viewModel.handleScrubUpdated(it.data)
            }

        }
    }

    override fun onSaveState(outState: UiSavedStateWriter) {
        stateSaver?.saveState(outState)
    }

    override fun onDestroy() {
        stateSaver = null
    }

    override fun onStateUpdated(state: QuoteViewState) {
        viewModel.handleUpdateQuoteWithChart(
            symbol = state.symbol,
            quote = state.quote,
            chart = state.chart
        )
    }

}
