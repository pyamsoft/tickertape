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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.requireAppBarActivity
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutCoordinatorBinding
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.watchlist.add.WatchlistAddDialog
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigDialog
import javax.inject.Inject

class WatchlistFragment : Fragment(), UiController<WatchListControllerEvent> {

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<WatchlistViewModel>(activity = true) {
    factory?.create(requireActivity())
  }

  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var spacer: WatchlistSpacer? = null

  @JvmField @Inject internal var list: WatchlistList? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.layout_coordinator, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val binding = LayoutCoordinatorBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusWatchlistComponent()
        .create(
            requireAppBarActivity(),
            requireToolbarActivity(),
            requireActivity(),
            viewLifecycleOwner,
            binding.layoutCoordinator)
        .inject(this)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            spacer.requireNotNull(),
            list.requireNotNull()) {
          return@createComponent when (it) {
            is WatchListViewEvent.ForceRefresh -> viewModel.handleFetchQuotes(true)
            is WatchListViewEvent.Remove -> viewModel.handleRemove(it.index)
            is WatchListViewEvent.Select -> viewModel.handleDigSymbol(it.index)
          }
        }

    viewModel.handleListenForAddEvents(viewLifecycleOwner.lifecycleScope)
  }

  override fun onControllerEvent(event: WatchListControllerEvent) {
    return when (event) {
      is WatchListControllerEvent.AddNewSymbol -> handleOpenSymbolAddDialog()
      is WatchListControllerEvent.ManageSymbol -> handleOpenDigDialog(event.quote.symbol)
    }
  }

  private fun handleOpenDigDialog(symbol: StockSymbol) {
      WatchlistDigDialog.newInstance(symbol).show(requireActivity(), WatchlistDigDialog.TAG)
    }

  private fun handleOpenSymbolAddDialog() {
    WatchlistAddDialog.newInstance().show(requireActivity(), WatchlistAddDialog.TAG)
  }

  override fun onStart() {
    super.onStart()
    viewModel.handleFetchQuotes(false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    spacer = null
    list = null
  }

  companion object {

    const val TAG = "WatchlistFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return WatchlistFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
