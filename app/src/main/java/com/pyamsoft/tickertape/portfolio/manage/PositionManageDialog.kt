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
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.app.makeFullscreen
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.db.holding.DbHolding
import javax.inject.Inject

internal class PositionManageDialog :
    AppCompatDialogFragment(), UiController<ManagePortfolioControllerEvent> {

  @JvmField @Inject internal var priceEntry: PositionPrice? = null

  @JvmField @Inject internal var numberOfSharesEntry: PositionShareCount? = null

  @JvmField @Inject internal var toolbar: PositionToolbar? = null

  @JvmField @Inject internal var list: PositionsList? = null

  @JvmField @Inject internal var holding: PositionHolding? = null

  @JvmField @Inject internal var factory: PositionManageViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<PositionManageViewModel> {
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
    makeFullscreen()

    val holdingId = DbHolding.Id(requireNotNull(requireArguments().getString(KEY_HOLDING_ID)))
    val binding = LayoutConstraintBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusPositionManageComponent()
        .create(this, requireActivity(), viewLifecycleOwner, binding.layoutConstraint, holdingId)
        .inject(this)

    val price = requireNotNull(priceEntry)
    val shareCount = requireNotNull(numberOfSharesEntry)
    val list = requireNotNull(list)
    val toolbar = requireNotNull(toolbar)
    val holding = requireNotNull(holding)
    val shadow =
        DropshadowView.createTyped<ManagePortfolioViewState, ManagePortfolioViewEvent>(
            binding.layoutConstraint)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            price,
            shareCount,
            holding,
            list,
            toolbar,
            shadow) {
          return@createComponent when (it) {
            is ManagePortfolioViewEvent.Close -> dismiss()
            is ManagePortfolioViewEvent.ForceRefresh -> viewModel.handleFetchPortfolio(true)
            is ManagePortfolioViewEvent.Remove -> viewModel.handleRemove(it.index)
            is ManagePortfolioViewEvent.UpdateNumberOfShares ->
                viewModel.handleUpdateNumberOfShares(it.number)
            is ManagePortfolioViewEvent.UpdateSharePrice ->
                viewModel.handleUpdateSharePrice(it.price)
          }
        }

    binding.layoutConstraint.layout {
      toolbar.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      shadow.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      holding.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      shareCount.also {
        connect(it.id(), ConstraintSet.TOP, holding.id(), ConstraintSet.BOTTOM)
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

      list.also {
        connect(it.id(), ConstraintSet.TOP, shareCount.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }
  }

  override fun onControllerEvent(event: ManagePortfolioControllerEvent) {
    return when (event) {
      is ManagePortfolioControllerEvent.Close -> dismiss()
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.handleFetchPortfolio(false)
  }

  companion object {

    private const val KEY_HOLDING_ID = "key_holding_id"
    const val TAG = "PositionManageDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(holdingId: DbHolding.Id): DialogFragment {
      return PositionManageDialog().apply {
        arguments = Bundle().apply { putString(KEY_HOLDING_ID, holdingId.id) }
      }
    }
  }
}
