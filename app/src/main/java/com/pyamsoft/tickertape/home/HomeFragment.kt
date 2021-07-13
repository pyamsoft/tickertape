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
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.main.MainPage
import javax.inject.Inject

class HomeFragment : Fragment(), UiController<HomeControllerEvent> {

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<HomeViewModel>(activity = true) {
    factory?.create(requireActivity())
  }

  @Inject @JvmField internal var scrollContainer: HomeScrollContainer? = null

  @Inject @JvmField internal var nestedContainer: HomeContainer? = null

  @Inject @JvmField internal var spacer: HomeSpacer? = null

  @Inject @JvmField internal var nestedBottomSpacer: HomeBottomSpacer? = null

  @Inject @JvmField internal var nestedIndexes: HomeIndexList? = null

  @Inject @JvmField internal var nestedPortfolio: HomePortfolio? = null

  @Inject @JvmField internal var nestedWatchlistTitle: HomeWatchlistTitle? = null

  @Inject @JvmField internal var nestedWatchlist: HomeWatchlist? = null

  @Inject @JvmField internal var nestedGainers: HomeGainerList? = null

  @Inject @JvmField internal var nestedLosers: HomeLoserList? = null

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

    val binding = LayoutCoordinatorBinding.bind(view)
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

    val container = nestedContainer.requireNotNull()
    container.nest(
        nestedPortfolio.requireNotNull(),
        nestedWatchlistTitle.requireNotNull(),
        nestedWatchlist.requireNotNull(),
        nestedIndexes.requireNotNull(),
        nestedGainers.requireNotNull(),
        nestedLosers.requireNotNull(),
        nestedBottomSpacer.requireNotNull())

    val scrollContainer = scrollContainer.requireNotNull()
    scrollContainer.nest(container)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            spacer.requireNotNull(),
            scrollContainer
        ) {
          return@createComponent when (it) {
            is HomeViewEvent.OpenPortfolio -> viewModel.handleOpenPage(MainPage.Portfolio)
            is HomeViewEvent.OpenWatchlist -> viewModel.handleOpenPage(MainPage.WatchList)
          }
        }
  }

  override fun onControllerEvent(event: HomeControllerEvent) {}

  override fun onStart() {
    super.onStart()
    viewModel.handleFetchIndexes(false)
    viewModel.handleFetchPortfolio(false)
    viewModel.handleFetchWatchlist(false)
    viewModel.handleFetchGainers(false)
    viewModel.handleFetchLosers(false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    scrollContainer = null
    nestedContainer = null
    nestedIndexes = null
    nestedPortfolio = null
    nestedWatchlist = null
    nestedWatchlistTitle = null
    nestedGainers = null
    nestedLosers = null
    nestedBottomSpacer = null
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
