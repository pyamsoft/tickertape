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
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.add.date.PositionAddDateDialog
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddScreen
import com.pyamsoft.tickertape.portfolio.dig.position.add.PositionAddViewModeler
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.asSymbol
import java.time.LocalDate
import javax.inject.Inject
import timber.log.Timber

internal class PositionAddDialog : AppCompatDialogFragment() {

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

  private fun handleSubmit() {
    viewModel.requireNotNull().handleSubmit(scope = viewLifecycleOwner.lifecycleScope)
  }

  private fun handleDateOfPurchaseClicked(
      positionId: DbPosition.Id,
      date: LocalDate?,
  ) {
    Timber.d("Handle DoP clicked: $date")
    PositionAddDateDialog.show(
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
        .plusPositionAddComponent()
        .create(
            getSymbol(),
            getHoldingId(),
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

          TickerTapeTheme(themeProvider) {
            PositionAddScreen(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                symbol = symbol,
                onPriceChanged = { vm.handlePriceChanged(it) },
                onNumberChanged = { vm.handleNumberChanged(it) },
                onSubmit = { handleSubmit() },
                onClose = { dismiss() },
                onDateOfPurchaseClicked = {
                  handleDateOfPurchaseClicked(
                      positionId = positionId,
                      date = it,
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

    viewModel.requireNotNull().restoreState(savedInstanceState)
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

    private const val KEY_SYMBOL = "key_symbol"
    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val TAG = "PositionAddDialog"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        symbol: StockSymbol,
        holdingId: DbHolding.Id,
    ): DialogFragment {
      return PositionAddDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.symbol())
              putString(KEY_HOLDING_ID, holdingId.id)
            }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        symbol: StockSymbol,
        holdingId: DbHolding.Id,
    ) {
      newInstance(symbol, holdingId).show(activity, TAG)
    }
  }
}
