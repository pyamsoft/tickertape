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

package com.pyamsoft.tickertape.portfolio.dig.position

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.app.makeFullWidth
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddScreen
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddViewModeler
import com.pyamsoft.tickertape.portfolio.dig.position.date.PositionDateDialog
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDate
import javax.inject.Inject
import timber.log.Timber

internal class PositionDialog : AppCompatDialogFragment() {

  @JvmField @Inject internal var viewModel: PositionAddViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireArguments()
        .getString(KEY_SYMBOL)
        .let { it.requireNotNull { "Must be created with $KEY_SYMBOL" } }
        .asSymbol()
  }

  @CheckResult
  private fun getHoldingId(): DbHolding.Id {
    return requireArguments()
        .getString(KEY_HOLDING_ID)
        .let { it.requireNotNull { "Must be created with $KEY_HOLDING_ID" } }
        .let { DbHolding.Id(it) }
  }

  @CheckResult
  private fun getExistingPositionId(): DbPosition.Id {
    return requireArguments()
        .getString(KEY_EXISTING_POSITION_ID)
        .let { it.requireNotNull { "Must be created with $KEY_EXISTING_POSITION_ID" } }
        .let { DbPosition.Id(it) }
  }

  @CheckResult
  private fun getHoldingType(): EquityType {
    return requireArguments()
        .getString(KEY_HOLDING_TYPE)
        .let { it.requireNotNull { "Must be created with $KEY_HOLDING_TYPE" } }
        .let { EquityType.valueOf(it) }
  }

  private fun handleSubmit() {
    viewModel
        .requireNotNull()
        .handleSubmit(
            scope = viewLifecycleOwner.lifecycleScope,
            onClose = { dismiss() },
        )
  }

  private fun handleDateOfPurchaseClicked(
      positionId: DbPosition.Id,
      date: LocalDate?,
  ) {
    Timber.d("Handle DoP clicked: $positionId $date")
    PositionDateDialog.show(
        activity = requireActivity(),
        positionId = positionId,
        purchaseDate = date,
    )
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val act = requireActivity()
    Injector.obtainFromApplication<TickerComponent>(act)
        .plusPositionComponent()
        .create(
            getSymbol(),
            getHoldingId(),
            getHoldingType(),
            getExistingPositionId(),
        )
        .inject(this)

    val vm = viewModel.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.dialog_position_add

      setContent {
        val symbol = remember { getSymbol() }

        vm.Render { state ->
          val positionId = state.positionId

          act.TickerTapeTheme(themeProvider) {
            PositionAddScreen(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                symbol = symbol,
                onPriceChanged = { vm.handlePriceChanged(it) },
                onNumberChanged = { vm.handleNumberChanged(it) },
                onSubmit = { handleSubmit() },
                onClose = { dismiss() },
                onDateOfPurchaseClicked = { date ->
                  handleDateOfPurchaseClicked(
                      positionId = positionId,
                      date = date,
                  )
                },
            )
          }
        }
      }
    }
  }

  override fun onViewCreated(
      view: View,
      savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    makeFullWidth()

    viewModel.requireNotNull().also { vm ->
      vm.restoreState(savedInstanceState)
      vm.bind(scope = viewLifecycleOwner.lifecycleScope)
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    makeFullWidth()
    recompose()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    viewModel = null
    theming = null
  }

  companion object {

    private val TAG = PositionDialog::class.java.name
    private const val KEY_SYMBOL = "key_symbol"
    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val KEY_HOLDING_TYPE = "key_holding_type"
    private const val KEY_EXISTING_POSITION_ID = "key_existing_position_id"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        symbol: StockSymbol,
        holdingId: DbHolding.Id,
        holdingType: EquityType,
        existingPositionId: DbPosition.Id,
    ): DialogFragment {
      return PositionDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.raw)
              putString(KEY_HOLDING_ID, holdingId.raw)
              putString(KEY_HOLDING_TYPE, holdingType.name)
              putString(KEY_EXISTING_POSITION_ID, existingPositionId.raw)
            }
      }
    }

    @JvmStatic
    fun create(
        activity: FragmentActivity,
        symbol: StockSymbol,
        holdingId: DbHolding.Id,
        holdingType: EquityType,
    ) {
      newInstance(
              symbol,
              holdingId,
              holdingType,
              existingPositionId = DbPosition.Id.EMPTY,
          )
          .show(activity, TAG)
    }

    @JvmStatic
    fun update(
        activity: FragmentActivity,
        symbol: StockSymbol,
        holdingId: DbHolding.Id,
        holdingType: EquityType,
        existingPositionId: DbPosition.Id,
    ) {
      newInstance(
              symbol,
              holdingId,
              holdingType,
              existingPositionId,
          )
          .show(activity, TAG)
    }
  }
}
