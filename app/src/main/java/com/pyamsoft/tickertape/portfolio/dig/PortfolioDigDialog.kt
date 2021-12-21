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

package com.pyamsoft.tickertape.portfolio.dig

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
import com.pyamsoft.tickertape.stocks.api.*
import javax.inject.Inject
import timber.log.Timber

internal class PortfolioDigDialog : AppCompatDialogFragment() {

  @JvmField @Inject internal var viewModel: PortfolioDigViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null

  private fun handleRangeSelected(range: StockChart.IntervalRange) {
    viewModel
        .requireNotNull()
        .handleRangeSelected(
            scope = viewLifecycleOwner.lifecycleScope,
            range = range,
        )
  }

  private fun handleRefresh(force: Boolean) {
    viewModel
        .requireNotNull()
        .handleLoadTicker(
            scope = viewLifecycleOwner.lifecycleScope,
            force = force,
        )
  }

  private fun handleAddPosition() {
    val holdingId = getHoldingId()
    Timber.d("Add new position to holding: $holdingId")
    // TODO
  }

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
  private fun getCurrentPrice(): StockMoneyValue? {
    return requireArguments().getDouble(KEY_CURRENT_PRICE, -1.0).let { v ->
      if (v.compareTo(0) >= 0) {
        v.asMoney()
      } else {
        null
      }
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    val act = requireActivity()
    Injector.obtainFromApplication<TickerComponent>(act)
        .plusPortfolioDigComponent()
        .create(
            getSymbol(),
            getHoldingId(),
        )
        .inject(this)

    val vm = viewModel.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    return ComposeView(act).apply {
      id = R.id.dialog_watchlist_dig

      setContent {
        val currentPrice = remember { getCurrentPrice() }

        vm.Render { state ->
          TickerTapeTheme(themeProvider) {
            PortfolioDigScreen(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                currentPrice = currentPrice,
                onClose = { dismiss() },
                onScrub = { vm.handleDateScrubbed(it) },
                onRangeSelected = { handleRangeSelected(it) },
                onTabUpdated = { vm.handleTabUpdated(it) },
                onRefresh = { handleRefresh(true) },
                onAddPosition = { handleAddPosition() },
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
    handleRefresh(false)
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
    private const val KEY_CURRENT_PRICE = "key_current_price"
    private const val TAG = "WatchlistDigDialog"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        holding: DbHolding,
        currentPrice: StockMoneyValue?,
    ): DialogFragment {
      return PortfolioDigDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, holding.symbol().symbol())
              putString(KEY_HOLDING_ID, holding.id().id)
              currentPrice?.also { putDouble(KEY_CURRENT_PRICE, it.value()) }
            }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        holding: DbHolding,
        currentPrice: StockMoneyValue?,
    ) {
      newInstance(holding, currentPrice).show(activity, TAG)
    }
  }
}
