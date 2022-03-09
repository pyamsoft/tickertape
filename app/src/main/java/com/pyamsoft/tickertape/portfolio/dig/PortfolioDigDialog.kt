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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
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
import com.pyamsoft.tickertape.portfolio.dig.position.PositionAddDialog
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockChart
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockOptionsQuote
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.stocks.api.asSymbol
import javax.inject.Inject

internal class PortfolioDigDialog : AppCompatDialogFragment() {

  @JvmField @Inject internal var viewModel: PortfolioDigViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

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
    PositionAddDialog.show(
        activity = requireActivity(),
        symbol = getSymbol(),
        holdingId = getHoldingId(),
        holdingType = getHoldingType(),
    )
  }

  private fun handleDeletePosition(position: DbPosition) {
    viewModel
        .requireNotNull()
        .handleDeletePosition(
            scope = viewLifecycleOwner.lifecycleScope,
            position = position,
        )
  }

  private fun handleTabUpdated(section: PortfolioDigSections) {
    viewModel
        .requireNotNull()
        .handleTabUpdated(
            scope = viewLifecycleOwner.lifecycleScope,
            section = section,
        )
  }

  @CheckResult
  private fun getSymbol(): StockSymbol {
    return requireArguments()
        .getString(KEY_SYMBOL)
        .let { it.requireNotNull { "Must be created with $KEY_SYMBOL" } }
        .asSymbol()
  }

  @CheckResult
  private fun getLookupSymbol(): StockSymbol? {
    return requireArguments().getString(KEY_LOOKUP_SYMBOL)?.asSymbol()
  }

  @CheckResult
  private fun getHoldingId(): DbHolding.Id {
    return requireArguments()
        .getString(KEY_HOLDING_ID)
        .let { it.requireNotNull { "Must be created with $KEY_HOLDING_ID" } }
        .let { DbHolding.Id(it) }
  }

  @CheckResult
  private fun getHoldingType(): EquityType {
    return requireArguments()
        .getString(KEY_HOLDING_TYPE)
        .let { it.requireNotNull { "Must be created with $KEY_HOLDING_TYPE" } }
        .let { EquityType.valueOf(it) }
  }

  @CheckResult
  private fun getHoldingSide(): TradeSide {
    return requireArguments()
        .getString(KEY_HOLDING_SIDE)
        .let { it.requireNotNull { "Must be created with $KEY_HOLDING_SIDE" } }
        .let { TradeSide.valueOf(it) }
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
            getLookupSymbol(),
            getHoldingId(),
            getHoldingType(),
            getHoldingSide(),
        )
        .inject(this)

    val vm = viewModel.requireNotNull()
    val loader = imageLoader.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    val currentPrice = getCurrentPrice()
    return ComposeView(act).apply {
      id = R.id.dialog_portfolio_dig

      setContent {
        vm.Render { state ->
          act.TickerTapeTheme(themeProvider) {
            PortfolioDigScreen(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                imageLoader = loader,
                currentPrice = currentPrice,
                onClose = { dismiss() },
                onScrub = { vm.handleDateScrubbed(it) },
                onRangeSelected = { handleRangeSelected(it) },
                onTabUpdated = { handleTabUpdated(it) },
                onRefresh = { handleRefresh(true) },
                onAddPosition = { handleAddPosition() },
                onDeletePosition = { handleDeletePosition(it) },
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
    imageLoader = null
  }

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    private const val KEY_LOOKUP_SYMBOL = "key_lookup_symbol"
    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val KEY_HOLDING_TYPE = "key_holding_type"
    private const val KEY_HOLDING_SIDE = "key_holding_side"
    private const val KEY_CURRENT_PRICE = "key_current_price"
    private const val TAG = "PortfolioDigDialog"

    @JvmStatic
    @CheckResult
    private fun newInstance(
        holding: DbHolding,
        quote: StockQuote?,
        currentPrice: StockMoneyValue?,
    ): DialogFragment {
      val lookupSymbol =
          when (quote) {
            null -> null
            is StockOptionsQuote -> quote.underlyingSymbol()
            else -> quote.symbol()
          }
      return PortfolioDigDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, holding.symbol().symbol())
              putString(KEY_HOLDING_ID, holding.id().id)
              putString(KEY_HOLDING_TYPE, holding.type().name)
              putString(KEY_HOLDING_SIDE, holding.side().name)
              currentPrice?.also { putDouble(KEY_CURRENT_PRICE, it.value()) }
              lookupSymbol?.also { putString(KEY_LOOKUP_SYMBOL, it.symbol()) }
            }
      }
    }

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        holding: DbHolding,
        quote: StockQuote?,
        currentPrice: StockMoneyValue?,
    ) {
      newInstance(holding, quote, currentPrice).show(activity, TAG)
    }
  }
}
