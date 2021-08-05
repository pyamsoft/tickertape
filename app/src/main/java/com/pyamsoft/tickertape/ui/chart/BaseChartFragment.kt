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

package com.pyamsoft.tickertape.ui.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.databinding.LayoutFrameBinding
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.quote.ui.component.chart.StockChartContainer
import com.pyamsoft.tickertape.quote.ui.component.chart.StockChartControllerEvent
import com.pyamsoft.tickertape.quote.ui.component.chart.StockChartViewEvent
import com.pyamsoft.tickertape.quote.ui.component.chart.StockChartViewModel
import javax.inject.Inject

abstract class BaseChartFragment protected constructor() :
    Fragment(), UiController<StockChartControllerEvent> {

  @JvmField @Inject internal var container: StockChartContainer? = null

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by viewModels<StockChartViewModel> { factory.requireNotNull().create(this) }

  private var stateSaver: StateSaver? = null

  final override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_frame, container, false)
  }

  final override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    val binding = LayoutFrameBinding.bind(view)
    injectComponent(binding.layoutFrame)

    stateSaver =
        createComponent(
            savedInstanceState, viewLifecycleOwner, viewModel, this, container.requireNotNull()) {
          return@createComponent when (it) {
            is StockChartViewEvent.RangeUpdated -> viewModel.handleRangeUpdated(it.index)
            is StockChartViewEvent.Scrub -> viewModel.handleScrub(it.data)
            is StockChartViewEvent.Refresh -> viewModel.handleRefresh(true)
          }
        }

    viewModel.handleRefresh(false)
  }

  final override fun onControllerEvent(event: StockChartControllerEvent) {
    // TODO
  }

  final override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  final override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    container = null
  }

  protected abstract fun injectComponent(root: ViewGroup)
}
