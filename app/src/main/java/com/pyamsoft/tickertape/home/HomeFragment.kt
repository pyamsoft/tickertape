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

package com.pyamsoft.tickertape.home

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
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
import com.pyamsoft.tickertape.main.MainPage
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.watchlist.dig.WatchlistDigDialog
import javax.inject.Inject

class HomeFragment : Fragment(), UiController<HomeControllerEvent> {

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<HomeViewModel>(activity = true) {
    factory?.create(requireActivity())
  }

  @Inject @JvmField internal var container: HomeScrollContainer? = null

  @Inject @JvmField internal var spacer: HomeSpacer? = null

  private var stateSaver: StateSaver? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.layout_coordinator, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Animate layout changes
    val binding =
        LayoutCoordinatorBinding.bind(view).apply {
          layoutCoordinator.layoutTransition = LayoutTransition()
        }

    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusHomeComponent()
        .create(
            requireAppBarActivity(),
            requireToolbarActivity(),
            requireActivity(),
            viewLifecycleOwner)
        .plusHomeComponent()
        .create(binding.layoutCoordinator)
        .inject(this)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            spacer.requireNotNull(),
            container.requireNotNull()) {
          return@createComponent when (it) {
            is HomeViewEvent.OpenPortfolio -> viewModel.handleOpenPage(MainPage.Portfolio)
            is HomeViewEvent.OpenWatchlist -> viewModel.handleOpenPage(MainPage.WatchList)
            is HomeViewEvent.DigDeeperWatchlist -> viewModel.handleDigWatchlistSymbol(it.index)
          }
        }
  }

  override fun onControllerEvent(event: HomeControllerEvent) {
    return when (event) {
      is HomeControllerEvent.ManageWatchlistSymbol -> handleOpenDigDialog(event.quote.symbol)
    }
  }

  private fun handleOpenDigDialog(symbol: StockSymbol) {
    WatchlistDigDialog.newInstance(symbol).show(requireActivity(), WatchlistDigDialog.TAG)
  }

  override fun onStart() {
    super.onStart()
    viewModel.apply {
      handleFetchIndexes(false)
      handleFetchPortfolio(false)
      handleFetchWatchlist(false)
      handleFetchGainers(false)
      handleFetchLosers(false)
      handleFetchTrending(false)
      handleFetchMostShorted(false)
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
    spacer = null
  }

  companion object {

    const val TAG = "HomeFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return HomeFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
