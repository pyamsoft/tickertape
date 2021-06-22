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
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsCommit
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsControllerEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsList
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsPriceEntry
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsShareCountEntry
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsViewEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsViewModel
import javax.inject.Inject

internal class PositionsFragment : Fragment(), UiController<PositionsControllerEvent> {

  @JvmField @Inject internal var priceEntry: PositionsPriceEntry? = null

  @JvmField @Inject internal var numberOfSharesEntry: PositionsShareCountEntry? = null

  @JvmField @Inject internal var list: PositionsList? = null

  @JvmField @Inject internal var commit: PositionsCommit? = null

  @JvmField @Inject internal var factory: PositionsViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<PositionsViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  private var stateSaver: StateSaver? = null

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

    val binding = LayoutConstraintBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(view.context)
    PositionManageDialog.getInjector(this)
        .plusPositionsComponent()
        .create(this, viewLifecycleOwner, binding.layoutConstraint)
        .inject(this)

    val price = requireNotNull(priceEntry)
    val shareCount = requireNotNull(numberOfSharesEntry)
    val commit = requireNotNull(commit)
    val list = requireNotNull(list)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            price,
            shareCount,
            commit,
            list,
        ) {
          return@createComponent when (it) {
            is PositionsViewEvent.ForceRefresh -> viewModel.handleFetchPortfolio(true)
            is PositionsViewEvent.Remove -> viewModel.handleRemove(it.index)
            is PositionsViewEvent.UpdateNumberOfShares ->
                viewModel.handleUpdateNumberOfShares(it.number)
            is PositionsViewEvent.UpdateSharePrice -> viewModel.handleUpdateSharePrice(it.price)
            is PositionsViewEvent.Commit -> viewModel.handleCreatePosition()
          }
        }

    binding.layoutConstraint.layout {
      shareCount.also {
          connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, price.id(), ConstraintSet.START)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
        setHorizontalWeight(it.id(), 1F)
      }

      price.also {
        connect(it.id(), ConstraintSet.TOP, shareCount.id(), ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, shareCount.id(), ConstraintSet.END)
        connect(it.id(), ConstraintSet.END, commit.id(), ConstraintSet.START)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
        setHorizontalWeight(it.id(), 1F)
      }

      commit.also {
        connect(it.id(), ConstraintSet.TOP, shareCount.id(), ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, price.id(), ConstraintSet.END)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.WRAP_CONTENT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      list.also {
        connect(it.id(), ConstraintSet.TOP, shareCount.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }
  }

  override fun onControllerEvent(event: PositionsControllerEvent) {}

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
    commit = null
    list = null
  }

  companion object {

    const val TAG = "PositionsFragment"

    @JvmStatic
    @CheckResult
    fun newInstance(): Fragment {
      return PositionsFragment().apply { arguments = Bundle().apply {} }
    }
  }
}
