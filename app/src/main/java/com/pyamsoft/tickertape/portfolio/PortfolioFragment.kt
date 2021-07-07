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

package com.pyamsoft.tickertape.portfolio

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
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.requireAppBarActivity
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutCoordinatorBinding
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.portfolio.add.PortfolioAddDialog
import com.pyamsoft.tickertape.portfolio.manage.PositionManageDialog
import javax.inject.Inject

class PortfolioFragment : Fragment(), UiController<PortfolioControllerEvent> {

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<PortfolioViewModel>(activity = true) {
    factory?.create(requireActivity())
  }

  private var stateSaver: StateSaver? = null

  @JvmField @Inject internal var header: PortfolioHeader? = null

  @JvmField @Inject internal var list: PortfolioList? = null

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
        .plusPortfolioComponent()
        .create(
            requireToolbarActivity(),
            requireAppBarActivity(),
            requireActivity(),
            viewLifecycleOwner)
        .plusPortfolioComponent()
        .create(binding.layoutCoordinator)
        .inject(this)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            requireNotNull(header),
            requireNotNull(list),
        ) {
          return@createComponent when (it) {
            is PortfolioViewEvent.ForceRefresh -> viewModel.handleFetchPortfolio(true)
            is PortfolioViewEvent.Remove -> viewModel.handleRemove(it.index)
            is PortfolioViewEvent.Manage -> viewModel.handleManageHolding(it.index)
          }
        }

    viewModel.handleListenForAddEvents(viewLifecycleOwner.lifecycleScope)
  }

  override fun onControllerEvent(event: PortfolioControllerEvent) {
    return when (event) {
      is PortfolioControllerEvent.AddNewHolding -> handleOpenHoldingAddDialog()
      is PortfolioControllerEvent.ManageHolding -> handleOpenHoldingManageDialog(event.stock)
    }
  }

  private fun handleOpenHoldingManageDialog(stock: PortfolioStock) {
    PositionManageDialog.newInstance(stock.holding)
        .show(requireActivity(), PositionManageDialog.TAG)
  }

  private fun handleOpenHoldingAddDialog() {
    PortfolioAddDialog.newInstance().show(requireActivity(), PortfolioAddDialog.TAG)
  }

  override fun onStart() {
    super.onStart()
    viewModel.handleFetchPortfolio(false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    stateSaver = null
    factory = null

    list = null
    header = null
  }

  companion object {

    const val TAG = "PortfolioFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return PortfolioFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
