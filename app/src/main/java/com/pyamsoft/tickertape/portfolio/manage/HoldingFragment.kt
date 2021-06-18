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

package com.pyamsoft.tickertape.portfolio.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.databinding.LayoutScrollingConstraintBinding
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingCommit
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingControllerEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingInfo
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingPriceEntry
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingQuote
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingShareCountEntry
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingSummary
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingViewEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.holding.HoldingViewModel
import javax.inject.Inject

internal class HoldingFragment : Fragment(), UiController<HoldingControllerEvent> {

  @JvmField @Inject internal var priceEntry: HoldingPriceEntry? = null

  @JvmField @Inject internal var numberOfSharesEntry: HoldingShareCountEntry? = null

  @JvmField @Inject internal var toolbar: ManagePortfolioToolbar? = null

  @JvmField @Inject internal var info: HoldingInfo? = null

  @JvmField @Inject internal var commit: HoldingCommit? = null

  @JvmField @Inject internal var quote: HoldingQuote? = null

  @JvmField @Inject internal var summary: HoldingSummary? = null

  @JvmField @Inject internal var factory: HoldingViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<HoldingViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  @JvmField @Inject internal var manageFactory: TickerViewModelFactory? = null
  private val manageViewModel by fromViewModelFactory<ManagePortfolioViewModel> {
    manageFactory?.create(this)
  }

  private var stateSaver: StateSaver? = null

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? {
    return inflater.inflate(R.layout.layout_scrolling_constraint, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    val binding = LayoutScrollingConstraintBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(view.context)
    PositionManageDialog.getInjector(this)
        .plusHoldingComponent()
        .create(this, binding.nestedConstraint)
        .inject(this)

    val price = requireNotNull(priceEntry)
    val shareCount = requireNotNull(numberOfSharesEntry)
    val info = requireNotNull(info)
    val quote = requireNotNull(quote)
    val commit = requireNotNull(commit)
    val summary = requireNotNull(summary)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            price,
            shareCount,
            info,
            commit,
            quote,
            summary,
        ) {
          return@createComponent when (it) {
            is HoldingViewEvent.ForceRefresh -> viewModel.handleFetchPortfolio(true)
            is HoldingViewEvent.Remove -> viewModel.handleRemove(it.index)
            is HoldingViewEvent.UpdateNumberOfShares ->
                viewModel.handleUpdateNumberOfShares(it.number)
            is HoldingViewEvent.UpdateSharePrice -> viewModel.handleUpdateSharePrice(it.price)
            is HoldingViewEvent.Commit -> viewModel.handleCreatePosition()
            is HoldingViewEvent.ListPositions -> pushPositionListFragment()
          }
        }

    binding.nestedConstraint.layout {
      info.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      quote.also {
        connect(it.id(), ConstraintSet.TOP, info.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      summary.also {
        connect(it.id(), ConstraintSet.TOP, quote.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      shareCount.also {
        connect(it.id(), ConstraintSet.TOP, summary.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, price.id(), ConstraintSet.START)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
        setHorizontalWeight(it.id(), 1F)
      }

      price.also {
        connect(it.id(), ConstraintSet.TOP, shareCount.id(), ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, shareCount.id(), ConstraintSet.END)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
        setHorizontalWeight(it.id(), 1F)
      }

      commit.also {
        connect(it.id(), ConstraintSet.TOP, shareCount.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }
    }
  }

  private fun pushPositionListFragment() {
    manageViewModel.handleLoadPositionsPage()
  }

  override fun onControllerEvent(event: HoldingControllerEvent) {}

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

    priceEntry = null
    numberOfSharesEntry = null
    toolbar = null
    info = null
    quote = null
    commit = null
    summary = null
  }

  companion object {

    const val TAG = "HoldingFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return HoldingFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
