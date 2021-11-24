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

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.asFactory
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.R as R2
import com.pyamsoft.pydroid.ui.app.requireAppBarActivity
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.databinding.LayoutCoordinatorBinding
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.watchlist.add.WatchlistAddDialog
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigDialog
import javax.inject.Inject

class WatchlistFragment : Fragment(), UiController<WatchListControllerEvent> {

  @JvmField @Inject internal var factory: WatchlistViewModel.Factory? = null
  private val viewModel by
      activityViewModels<WatchlistViewModel> { factory.requireNotNull().asFactory(this) }

  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var toolbar: WatchlistToolbar? = null

  @JvmField @Inject internal var tabs: WatchlistTabs? = null

  @JvmField @Inject internal var container: WatchlistScrollContainer? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R2.layout.layout_coordinator, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Animate layout changes
    val binding =
        LayoutCoordinatorBinding.bind(view).apply {
          layoutCoordinator.layoutTransition = LayoutTransition()
        }

    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusWatchlistComponent()
        .create(
            requireAppBarActivity(),
            requireToolbarActivity(),
            requireActivity(),
            viewLifecycleOwner)
        .plusWatchlistComponent()
        .create(binding.layoutCoordinator)
        .inject(this)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            toolbar.requireNotNull(),
            tabs.requireNotNull(),
            container.requireNotNull(),
        ) {
          return@createComponent when (it) {
            is WatchListViewEvent.ForceRefresh -> viewModel.handleFetchQuotes(true)
            is WatchListViewEvent.Remove -> viewModel.handleRemove(it.index)
            is WatchListViewEvent.Select -> viewModel.handleDigSymbol(it.index)
            is WatchListViewEvent.ShowOptions -> viewModel.handleShowOptions()
            is WatchListViewEvent.ShowStocks -> viewModel.handleShowStocks()
            is WatchListViewEvent.ShowCrypto -> viewModel.handleShowCrypto()
            is WatchListViewEvent.Search -> viewModel.handleSearch(it.query)
          }
        }

    viewModel.handleListenForAddEvents(viewLifecycleOwner.lifecycleScope)
  }

  override fun onControllerEvent(event: WatchListControllerEvent) {
    return when (event) {
      is WatchListControllerEvent.AddNewSymbol -> handleOpenSymbolAddDialog(event.type, event.side)
      is WatchListControllerEvent.ManageSymbol -> handleOpenDigDialog(event.quote.symbol)
    }
  }

  private fun handleOpenDigDialog(symbol: StockSymbol) {
    WatchlistDigDialog.show(requireActivity(), symbol)
  }

  private fun handleOpenSymbolAddDialog(type: EquityType, side: TradeSide) {
    WatchlistAddDialog.newInstance(type, side).show(requireActivity(), WatchlistAddDialog.TAG)
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

    tabs = null
    container = null
    toolbar = null
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return WatchlistFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
