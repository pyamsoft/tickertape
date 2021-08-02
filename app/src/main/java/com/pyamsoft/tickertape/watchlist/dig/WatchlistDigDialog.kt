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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullscreen
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutLinearVerticalBinding
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.watchlist.dig.chart.WatchlistDigChartFragment
import javax.inject.Inject

internal class WatchlistDigDialog :
    AppCompatDialogFragment(), UiController<BaseWatchListDigControllerEvent> {

  @JvmField @Inject internal var toolbar: BaseWatchlistDigToolbar? = null

  @JvmField @Inject internal var container: BaseWatchlistDigContainer? = null

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<BaseWatchlistDigViewModel> { factory?.create(this) }

  private var component: BaseWatchlistDigComponent? = null

  private var stateSaver: StateSaver? = null

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireNotNull(requireArguments().getString(KEY_SYMBOL)).asSymbol()
  }

  private fun eatBackButtonPress() {
    requireActivity()
        .onBackPressedDispatcher
        .addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
              override fun handleOnBackPressed() {
                val fm = childFragmentManager
                if (fm.backStackEntryCount > 0) {
                  fm.popBackStack()

                  // Upon popping, we now restore the default page toolbar state
                  viewModel.handleLoadDefaultPage()
                } else {
                  dismiss()
                }
              }
            })
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return object : AppCompatDialog(requireActivity(), theme) {

      override fun onBackPressed() {
        requireActivity().onBackPressed()
      }
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_linear_vertical, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullscreen()
    eatBackButtonPress()

    val binding = LayoutLinearVerticalBinding.bind(view)
    component =
        Injector.obtainFromApplication<TickerComponent>(view.context)
            .plusWatchlistDigComponent()
            .create(getSymbol())
            .also { c -> c.plusDigComponent().create(binding.layoutLinearV).inject(this) }

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            toolbar.requireNotNull(),
            container.requireNotNull()) {
          return@createComponent when (it) {
            is BaseWatchListDigViewEvent.Close -> requireActivity().onBackPressed()
          }
        }

    viewModel.handleLoadDefaultPage()
  }

  private fun pushFragment(fragment: Fragment, tag: String, appendBackStack: Boolean) {
    val fm = childFragmentManager
    val containerId = container.requireNotNull().id()
    val existing = fm.findFragmentById(containerId)
    if (existing == null || existing.tag !== tag) {
      fm.commit(viewLifecycleOwner) {
        if (appendBackStack) {
          addToBackStack(null)
        }

        replace(containerId, fragment, tag)
      }
    }
  }

  override fun onControllerEvent(event: BaseWatchListDigControllerEvent) {
    return when (event) {
      is BaseWatchListDigControllerEvent.PushQuote ->
          pushFragment(
              fragment = WatchlistDigChartFragment.newInstance(),
              tag = WatchlistDigChartFragment.TAG,
              appendBackStack = false)
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    container = null
    toolbar = null
    component = null
  }

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    const val TAG = "WatchlistDigDialog"

    @JvmStatic
    @CheckResult
    fun getInjector(fragment: Fragment): BaseWatchlistDigComponent {
      val parent = fragment.parentFragment
      if (parent is WatchlistDigDialog) {
        return parent.component.requireNotNull()
      }

      throw AssertionError(
          "Cannot call getInjector() from a fragment that does not use WatchlistDigDialog as it's parent.")
    }

    @JvmStatic
    @CheckResult
    fun newInstance(symbol: StockSymbol): DialogFragment {
      return WatchlistDigDialog().apply {
        arguments = Bundle().apply { putString(KEY_SYMBOL, symbol.symbol()) }
      }
    }
  }
}
