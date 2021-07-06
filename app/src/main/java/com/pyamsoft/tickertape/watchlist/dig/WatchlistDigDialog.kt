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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullscreen
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import javax.inject.Inject

internal class WatchlistDigDialog :
    AppCompatDialogFragment(), UiController<WatchListDigControllerEvent> {

  @JvmField @Inject internal var chart: WatchlistDigChart? = null

  @JvmField @Inject internal var toolbar: WatchlistDigToolbar? = null

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<WatchlistDigViewModel> { factory?.create(this) }

  private var stateSaver: StateSaver? = null

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireNotNull(requireArguments().getString(KEY_SYMBOL)).asSymbol()
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullscreen()

    val binding = LayoutConstraintBinding.bind(view)
        Injector.obtainFromApplication<TickerComponent>(view.context)
            .plusWatchlistDigComponent()
            .create(
              this,
              viewLifecycleOwner,
              viewModelStore,
              getSymbol(),
              binding.layoutConstraint
            )
          .inject(this)

    val chart = requireNotNull(chart)
    val toolbar = requireNotNull(toolbar)
    val shadow =
        DropshadowView.createTyped<WatchListDigViewState, WatchListDigViewEvent>(
            binding.layoutConstraint)

    stateSaver =
        createComponent(
            savedInstanceState, viewLifecycleOwner, viewModel, this, chart, toolbar, shadow) {
          return@createComponent when (it) {
            is WatchListDigViewEvent.Close -> dismiss()
          }
        }

    binding.layoutConstraint.layout {
      toolbar.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
      }

      shadow.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
      }

      chart.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
      }
    }

    if (savedInstanceState == null) {
      viewModel.handleFetchQuote(false, StockChart.IntervalRange.ONE_DAY)
    }
  }
  override fun onControllerEvent(event: WatchListDigControllerEvent) {
      // TODO
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    toolbar = null
    chart = null
  }

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    const val TAG = "WatchlistDigDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(symbol: StockSymbol): DialogFragment {
      return WatchlistDigDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.symbol())
            }
      }
    }
  }
}
