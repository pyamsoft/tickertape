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

package com.pyamsoft.tickertape.portfolio.manage.add

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullWidth
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionAddToolbar
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddControllerEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddViewEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddViewModel
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddViewState
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsCommit
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsPriceEntry
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsShareCountEntry
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import javax.inject.Inject

internal class PositionsAddDialog :
    AppCompatDialogFragment(), UiController<PositionsAddControllerEvent> {

  @JvmField @Inject internal var priceEntry: PositionsPriceEntry? = null

  @JvmField @Inject internal var numberOfSharesEntry: PositionsShareCountEntry? = null

  @JvmField @Inject internal var commit: PositionsCommit? = null

  @JvmField @Inject internal var toolbar: PositionAddToolbar? = null

  @JvmField @Inject internal var factory: PositionsAddViewModel.Factory? = null
  private val viewModel by fromViewModelFactory<PositionsAddViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  private var stateSaver: StateSaver? = null

  @CheckResult
  private fun getHoldingId(): DbHolding.Id {
    return DbHolding.Id(requireNotNull(requireArguments().getString(KEY_HOLDING_ID)))
  }

  @CheckResult
  private fun getHoldingSymbol(): StockSymbol {
    return requireNotNull(requireArguments().getString(KEY_HOLDING_SYMBOL)).asSymbol()
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
    return inflater.inflate(R.layout.layout_constraint, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullWidth()
    eatBackButtonPress()

    val binding = LayoutConstraintBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusPositionAddComponent()
        .create(this, binding.layoutConstraint, getHoldingId(), getHoldingSymbol())
        .inject(this)

    val toolbar = requireNotNull(toolbar)
    val shareCount = requireNotNull(numberOfSharesEntry)
    val price = requireNotNull(priceEntry)
    val commit = requireNotNull(commit)
    val shadow =
        DropshadowView.createTyped<PositionsAddViewState, PositionsAddViewEvent>(
            binding.layoutConstraint)

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            shareCount,
            price,
            commit,
            toolbar,
            shadow) {
          return@createComponent when (it) {
            is PositionsAddViewEvent.UpdateNumberOfShares ->
                viewModel.handleUpdateNumberOfShares(it.number)
            is PositionsAddViewEvent.UpdateSharePrice -> viewModel.handleUpdateSharePrice(it.price)
            is PositionsAddViewEvent.Commit -> viewModel.handleCreatePosition()
            is PositionsAddViewEvent.Close -> requireActivity().onBackPressed()
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

      shareCount.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
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
    }
  }

  private fun eatBackButtonPress() {
    requireActivity()
        .onBackPressedDispatcher
        .addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
              override fun handleOnBackPressed() {
                dismiss()
              }
            })
  }

  override fun onControllerEvent(event: PositionsAddControllerEvent) {}

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
    toolbar = null
  }

  companion object {

    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val KEY_HOLDING_SYMBOL = "key_holding_symbol"
    const val TAG = "PositionAddDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(id: DbHolding.Id, symbol: StockSymbol): DialogFragment {
      return PositionsAddDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_HOLDING_ID, id.id)
              putString(KEY_HOLDING_SYMBOL, symbol.symbol())
            }
      }
    }
  }
}
