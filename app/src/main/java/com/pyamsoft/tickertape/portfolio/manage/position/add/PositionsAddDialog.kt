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

package com.pyamsoft.tickertape.portfolio.manage.position.add

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
import androidx.fragment.app.viewModels
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.asFactory
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullWidth
import com.pyamsoft.pydroid.ui.databinding.LayoutLinearVerticalBinding
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionAddToolbar
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddContainer
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddControllerEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddViewEvent
import com.pyamsoft.tickertape.portfolio.manage.positions.add.PositionsAddViewModel
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import com.pyamsoft.tickertape.stocks.api.fromHoldingString
import com.pyamsoft.tickertape.stocks.api.toHoldingString
import java.time.LocalDateTime
import javax.inject.Inject

internal class PositionsAddDialog :
    AppCompatDialogFragment(), UiController<PositionsAddControllerEvent> {

  @JvmField @Inject internal var container: PositionsAddContainer? = null

  @JvmField @Inject internal var toolbar: PositionAddToolbar? = null

  @JvmField @Inject internal var factory: PositionsAddViewModel.Factory? = null
  private val viewModel by viewModels<PositionsAddViewModel> {
    factory.requireNotNull().asFactory(this)
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

  @CheckResult
  private fun getHoldingType(): HoldingType {
    return requireArguments().getString(KEY_HOLDING_TYPE, "").requireNotNull().fromHoldingString()
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
    return inflater.inflate(R.layout.layout_linear_vertical, container, false)
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullWidth()
    eatBackButtonPress()

    val binding = LayoutLinearVerticalBinding.bind(view)
    Injector.obtainFromApplication<TickerComponent>(requireActivity())
        .plusPositionAddComponent()
        .create(
            this,
            binding.layoutLinearV,
            getHoldingId(),
            getHoldingSymbol(),
            getHoldingType(),
        )
        .inject(this)

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
            is PositionsAddViewEvent.UpdateNumberOfShares ->
                viewModel.handleUpdateNumberOfShares(it.number)
            is PositionsAddViewEvent.UpdateSharePrice -> viewModel.handleUpdateSharePrice(it.price)
            is PositionsAddViewEvent.Commit -> viewModel.handleCreatePosition()
            is PositionsAddViewEvent.Close -> requireActivity().onBackPressed()
            is PositionsAddViewEvent.OpenDatePicker -> viewModel.handleDatePicker()
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

  override fun onControllerEvent(event: PositionsAddControllerEvent) {
    return when (event) {
      is PositionsAddControllerEvent.OpenDatePicker ->
          handleOpenDatePickerDialog(event.selectedDate)
    }
  }

  private fun handleOpenDatePickerDialog(selectedDate: LocalDateTime?) {
    PurchaseDatePickerDialog.newInstance(selectedDate)
        .show(requireActivity(), PurchaseDatePickerDialog.TAG)
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
    toolbar = null
  }

  companion object {

    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val KEY_HOLDING_SYMBOL = "key_holding_symbol"
    private const val KEY_HOLDING_TYPE = "key_holding_type"
    const val TAG = "PositionAddDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(id: DbHolding.Id, symbol: StockSymbol, type: HoldingType): DialogFragment {
      return PositionsAddDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_HOLDING_ID, id.id)
              putString(KEY_HOLDING_SYMBOL, symbol.symbol())
              putString(KEY_HOLDING_TYPE, type.toHoldingString())
            }
      }
    }
  }
}
