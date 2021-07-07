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
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullscreen
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.portfolio.manage.position.add.PositionsAddDialog
import com.pyamsoft.tickertape.portfolio.manage.position.PositionsFragment
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import javax.inject.Inject

internal class PositionManageDialog :
    AppCompatDialogFragment(), UiController<ManagePortfolioControllerEvent> {

  @JvmField @Inject internal var toolbar: ManagePortfolioToolbar? = null

  @JvmField @Inject internal var container: ManagePortfolioContainer? = null

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by fromViewModelFactory<ManagePortfolioViewModel> { factory?.create(this) }

  private var component: BaseManageComponent? = null

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
    makeFullscreen()
    eatBackButtonPress()

    val binding = LayoutConstraintBinding.bind(view)
    component =
        Injector.obtainFromApplication<TickerComponent>(view.context)
            .plusManageComponent()
            .create(getHoldingSymbol(), getHoldingId())
            .also { c ->
              c.plusPositionManageComponent().create(binding.layoutConstraint).inject(this)
            }

    val container = requireNotNull(container)
    val toolbar = requireNotNull(toolbar)
    val shadow =
        DropshadowView.createTyped<ManagePortfolioViewState, ManagePortfolioViewEvent>(
            binding.layoutConstraint)

    stateSaver =
        createComponent(
            savedInstanceState, viewLifecycleOwner, viewModel, this, container, toolbar, shadow) {
          return@createComponent when (it) {
            is ManagePortfolioViewEvent.Close -> requireActivity().onBackPressed()
            is ManagePortfolioViewEvent.Add -> viewModel.handleOpenAddDialog()
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

      container.also {
        connect(it.id(), ConstraintSet.TOP, toolbar.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
      }
    }

    if (savedInstanceState == null) {
      viewModel.handleLoadDefaultPage()
    }
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

  private fun pushFragment(fragment: Fragment, tag: String, appendBackStack: Boolean) {
    val fm = childFragmentManager
    val containerId = requireNotNull(container).id()
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

  override fun onControllerEvent(event: ManagePortfolioControllerEvent) {
    return when (event) {
      is ManagePortfolioControllerEvent.PushPositions ->
          pushFragment(
              PositionsFragment.newInstance(), PositionsFragment.TAG, appendBackStack = false)
      is ManagePortfolioControllerEvent.OpenAdd ->
          PositionsAddDialog.newInstance(id = event.id, symbol = event.symbol)
              .show(requireActivity(), PositionsAddDialog.TAG)
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

    toolbar = null
    container = null

    component = null
  }

  companion object {

    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val KEY_HOLDING_SYMBOL = "key_holding_symbol"
    const val TAG = "PositionManageDialog"

    @JvmStatic
    @CheckResult
    fun getInjector(fragment: Fragment): BaseManageComponent {
      val parent = fragment.parentFragment
      if (parent is PositionManageDialog) {
        return requireNotNull(parent.component)
      }

      throw AssertionError(
          "Cannot call getInjector() from a fragment that does not use PositionManageDialog as it's parent.")
    }

    @JvmStatic
    @CheckResult
    fun newInstance(holding: DbHolding): DialogFragment {
      return PositionManageDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_HOLDING_ID, holding.id().id)
              putString(KEY_HOLDING_SYMBOL, holding.symbol().symbol())
            }
      }
    }
  }
}
