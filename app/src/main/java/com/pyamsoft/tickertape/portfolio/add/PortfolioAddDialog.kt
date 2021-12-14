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

package com.pyamsoft.tickertape.portfolio.add

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.pyamsoft.pydroid.arch.asFactory
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.TradeSide
import com.pyamsoft.tickertape.symbol.SymbolAddDialog
import javax.inject.Inject

internal class PortfolioAddDialog : SymbolAddDialog<PortfolioAddViewModel>() {

  @JvmField @Inject internal var factory: PortfolioAddViewModel.Factory? = null
  override val viewModel by
      viewModels<PortfolioAddViewModel> { factory.requireNotNull().asFactory(this) }

  override fun onInject(
      view: ViewGroup,
      savedInstanceState: Bundle?,
      equityType: EquityType,
      tradeSide: TradeSide
  ) {
    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusPortfolioAddComponent()
        .create(
            this,
            requireActivity(),
            viewLifecycleOwner,
        )
        .plusPortfolioAddComponent()
        .create(
            view,
            equityType,
            tradeSide,
        )
        .inject(this)
  }

  override fun onTeardown() {
    factory = null
  }

  companion object {

    const val TAG = "PortfolioAddDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(type: EquityType, side: TradeSide): DialogFragment {
      return PortfolioAddDialog().apply {
        arguments =
            Bundle().apply {
              putString(KEY_HOLDING_TYPE, type.name)
              putString(KEY_HOLDING_SIDE, side.name)
            }
      }
    }
  }
}
