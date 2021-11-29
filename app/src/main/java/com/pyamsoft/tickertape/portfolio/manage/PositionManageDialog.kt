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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullscreen
import com.pyamsoft.pydroid.ui.databinding.LayoutLinearVerticalBinding
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.core.TickerViewModelFactory
import com.pyamsoft.tickertape.core.isNegative
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.manage.chart.PositionChartFragment
import com.pyamsoft.tickertape.portfolio.manage.position.PositionsFragment
import com.pyamsoft.tickertape.portfolio.manage.position.add.PositionsAddDialog
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.ui.correctBackground
import javax.inject.Inject
import com.pyamsoft.pydroid.ui.R as R2

internal class PositionManageDialog :
    AppCompatDialogFragment(), UiController<ManagePortfolioControllerEvent> {

  @JvmField @Inject internal var toolbar: ManagePortfolioToolbar? = null

  @JvmField @Inject internal var container: ManagePortfolioContainer? = null

  @JvmField @Inject internal var factory: TickerViewModelFactory? = null
  private val viewModel by viewModels<ManagePortfolioViewModel> {
    factory.requireNotNull().create(this)
  }

  private var component: BaseManageComponent? = null

  private var stateSaver: StateSaver? = null

  @CheckResult
  private fun getHoldingId(): DbHolding.Id {
    return DbHolding.Id(requireArguments().getString(KEY_HOLDING_ID).requireNotNull())
  }

  @CheckResult
  private fun getHoldingSymbol(): StockSymbol {
    return requireArguments().getString(KEY_HOLDING_SYMBOL).requireNotNull().asSymbol()
  }

  @CheckResult
  private fun getHoldingType(): EquityType {
    return EquityType.valueOf(requireArguments().getString(KEY_HOLDING_TYPE, "").requireNotNull())
  }

  @CheckResult
  private fun getHoldingSide(): TradeSide {
    return TradeSide.valueOf(requireArguments().getString(KEY_HOLDING_SIDE, "").requireNotNull())
  }

  @CheckResult
  private fun getCurrentPrice(): StockMoneyValue? {
    val price = requireArguments().getDouble(KEY_CURRENT_STOCK_PRICE, -1.0)
    return if (price.isNegative()) null else price.asMoney()
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
    return inflater.inflate(R2.layout.layout_linear_vertical, container, false).apply {
        correctBackground(this)
    }
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullscreen()
    eatBackButtonPress()

    val binding = LayoutLinearVerticalBinding.bind(view)
    component =
        Injector.obtainFromApplication<TickerComponent>(view.context)
            .plusManageComponent()
            .create(
                getHoldingSymbol(),
                getHoldingId(),
                getCurrentPrice(),
                getHoldingType(),
                getHoldingSide(),
            )
            .also { c ->
              c.plusPositionManageComponent().create(binding.layoutLinearV).inject(this)
            }

    stateSaver =
        createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            this,
            toolbar.requireNotNull(),
            container.requireNotNull(),
        ) {
          return@createComponent when (it) {
            is ManagePortfolioViewEvent.Close -> requireActivity().onBackPressed()
            is ManagePortfolioViewEvent.Add -> viewModel.handleOpenAddDialog()
            is ManagePortfolioViewEvent.OpenPositions -> viewModel.handleLoadPositions()
            is ManagePortfolioViewEvent.OpenQuote -> viewModel.handleLoadQuote()
          }
        }

    viewModel.handleLoadDefaultPage()
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
    val containerId = container.requireNotNull().id()
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
      is ManagePortfolioControllerEvent.PushQuote ->
          pushFragment(
              PositionChartFragment.newInstance(),
              PositionChartFragment.TAG,
              appendBackStack = false)
      is ManagePortfolioControllerEvent.OpenAdd ->
          PositionsAddDialog.newInstance(
                  id = event.id,
                  symbol = event.symbol,
                  type = event.type,
                  side = event.side,
              )
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
    private const val KEY_HOLDING_TYPE = "key_holding_type"
    private const val KEY_HOLDING_SIDE = "key_holding_side"
    private const val KEY_CURRENT_STOCK_PRICE = "key_current_stock_price"
    private const val TAG = "PositionManageDialog"

    @JvmStatic
    @CheckResult
    fun getInjector(fragment: Fragment): BaseManageComponent {
      val parent = fragment.parentFragment
      if (parent is PositionManageDialog) {
        return parent.component.requireNotNull()
      }

      throw AssertionError(
          "Cannot call getInjector() from a fragment that does not use PositionManageDialog as it's parent.")
    }

    @JvmStatic
    @CheckResult
    private fun newInstance(stock: PortfolioStock, currentSharePrice: StockMoneyValue?): DialogFragment {
      return PositionManageDialog().apply {
        arguments =
            Bundle().apply {
              val holding = stock.holding
              putString(KEY_HOLDING_ID, holding.id().id)
              putString(KEY_HOLDING_SYMBOL, holding.symbol().symbol())
              putString(KEY_HOLDING_TYPE, holding.type().name)
              putString(KEY_HOLDING_SIDE, holding.side().name)
              currentSharePrice?.also { putDouble(KEY_CURRENT_STOCK_PRICE, it.value()) }
            }
      }
    }

      @JvmStatic
      fun show(activity: FragmentActivity, stock: PortfolioStock, currentSharePrice: StockMoneyValue?) {
          return newInstance(stock, currentSharePrice).show(activity, TAG)
      }
  }
}
