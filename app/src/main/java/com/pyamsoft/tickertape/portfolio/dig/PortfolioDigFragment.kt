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
import androidx.activity.compose.BackHandler
import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.navigator.BackstackNavigator
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.dispose
import com.pyamsoft.pydroid.ui.util.recompose
import com.pyamsoft.tickertape.R
import com.pyamsoft.tickertape.TickerTapeTheme
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.main.MainComponent
import com.pyamsoft.tickertape.main.MainPage
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

internal class PortfolioDigFragment : Fragment(), FragmentNavigator.Screen<MainPage> {

  @JvmField @Inject internal var navigator: BackstackNavigator<MainPage>? = null
  @JvmField @Inject internal var viewModel: PortfolioDigViewModeler? = null
  @JvmField @Inject internal var theming: Theming? = null
  @JvmField @Inject internal var imageLoader: ImageLoader? = null

  private var windowInsetObserver: ViewWindowInsetObserver? = null

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
    PositionAddDialog.create(
        activity = requireActivity(),
        symbol = getSymbol(),
        holdingId = getHoldingId(),
        holdingType = getHoldingType(),
    )
  }

  private fun handleUpdatePosition(position: DbPosition) {
    PositionAddDialog.update(
        activity = requireActivity(),
        symbol = getSymbol(),
        holdingId = getHoldingId(),
        holdingType = getHoldingType(),
        existingPositionId = position.id(),
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
    Injector.obtainFromActivity<MainComponent>(act)
        .plusPortfolioDig()
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
    val navi = navigator.requireNotNull()

    val themeProvider = ThemeProvider { theming.requireNotNull().isDarkTheme(act) }
    val currentPrice = getCurrentPrice()
    return ComposeView(act).apply {
      id = R.id.dialog_portfolio_dig

      val observer = ViewWindowInsetObserver(this)
      val windowInsets = observer.start()
      windowInsetObserver = observer

      setContent {
        vm.Render { state ->
          act.TickerTapeTheme(themeProvider) {
            CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
              BackHandler(
                  onBack = { navi.goBack() },
              )

              PortfolioDigScreen(
                  modifier = Modifier.fillMaxWidth(),
                  state = state,
                  imageLoader = loader,
                  currentPrice = currentPrice,
                  onClose = { act.onBackPressed() },
                  onScrub = { vm.handleDateScrubbed(it) },
                  onRangeSelected = { handleRangeSelected(it) },
                  onTabUpdated = { handleTabUpdated(it) },
                  onRefresh = { handleRefresh(true) },
                  onAddPosition = { handleAddPosition() },
                  onDeletePosition = { handleDeletePosition(it) },
                  onUpdatePosition = { handleUpdatePosition(it) },
              )
            }
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
    viewModel.requireNotNull().also { vm ->
      vm.restoreState(savedInstanceState)
      vm.bind(scope = viewLifecycleOwner.lifecycleScope)
    }

    handleRefresh(false)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    recompose()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    viewModel?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dispose()

    windowInsetObserver?.stop()
    windowInsetObserver = null

    viewModel = null
    theming = null
    imageLoader = null
    navigator = null
  }

  override fun getScreenId(): MainPage {
    return Screen(
        symbol = getSymbol(),
        lookupSymbol = getLookupSymbol(),
        holdingId = getHoldingId(),
        holdingType = getHoldingType(),
        holdingSide = getHoldingSide(),
        currentPrice = getCurrentPrice(),
    )
  }

  data class Screen
  internal constructor(
      val symbol: StockSymbol,
      val lookupSymbol: StockSymbol?,
      val holdingId: DbHolding.Id,
      val holdingType: EquityType,
      val holdingSide: TradeSide,
      val currentPrice: StockMoneyValue?,
  ) : MainPage {

    companion object {

      @JvmStatic
      @CheckResult
      fun create(
          holding: DbHolding,
          quote: StockQuote?,
          currentPrice: StockMoneyValue?,
      ): Screen {
        val lookupSymbol =
            when (quote) {
              null -> null
              is StockOptionsQuote -> quote.underlyingSymbol()
              else -> quote.symbol()
            }
        return Screen(
            symbol = holding.symbol(),
            lookupSymbol = lookupSymbol,
            holdingId = holding.id(),
            holdingType = holding.type(),
            holdingSide = holding.side(),
            currentPrice = currentPrice,
        )
      }
    }
  }

  companion object {

    private const val KEY_SYMBOL = "key_symbol"
    private const val KEY_LOOKUP_SYMBOL = "key_lookup_symbol"
    private const val KEY_HOLDING_ID = "key_holding_id"
    private const val KEY_HOLDING_TYPE = "key_holding_type"
    private const val KEY_HOLDING_SIDE = "key_holding_side"
    private const val KEY_CURRENT_PRICE = "key_current_price"

    @JvmStatic
    @CheckResult
    fun newInstance(
        symbol: StockSymbol,
        lookupSymbol: StockSymbol?,
        holdingId: DbHolding.Id,
        holdingType: EquityType,
        holdingSide: TradeSide,
        currentPrice: StockMoneyValue?,
    ): Fragment {

      return PortfolioDigFragment().apply {
        arguments =
            Bundle().apply {
              putString(KEY_SYMBOL, symbol.symbol())
              putString(KEY_HOLDING_ID, holdingId.id)
              putString(KEY_HOLDING_TYPE, holdingType.name)
              putString(KEY_HOLDING_SIDE, holdingSide.name)
              currentPrice?.also { putDouble(KEY_CURRENT_PRICE, it.value()) }
              lookupSymbol?.also { putString(KEY_LOOKUP_SYMBOL, it.symbol()) }
            }
      }
    }
  }
}
