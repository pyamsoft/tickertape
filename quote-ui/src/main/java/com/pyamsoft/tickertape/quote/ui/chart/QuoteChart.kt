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

import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.quote.ui.QuoteDelegateView
import javax.inject.Inject

class QuoteChart @Inject internal constructor(private val controller: QuoteChartController) :
    UiView<QuoteViewState, Nothing>(), QuoteDelegateView by controller {

  init {
    doOnInflate { controller.create(it) }
    doOnTeardown { controller.destroy() }
    doOnSaveState { controller.saveState(it) }
  }

  override fun render(state: UiRender<QuoteViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: QuoteViewState) {
    controller.handleUpdateState(state)
  }
}