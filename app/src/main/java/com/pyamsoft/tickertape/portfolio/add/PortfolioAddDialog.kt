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
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.symbol.SymbolAddDialog
import javax.inject.Inject

internal class PortfolioAddDialog : SymbolAddDialog<PortfolioAddViewModel>() {

  @JvmField @Inject internal var factory: PortfolioAddViewModel.Factory? = null
  override val viewModel by fromViewModelFactory<PortfolioAddViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  override fun onInject(view: ViewGroup, savedInstanceState: Bundle?) {
    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusPortfolioAddComponent()
        .create(
            this,
            requireActivity(),
            viewLifecycleOwner,
            view,
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
    fun newInstance(): DialogFragment {
      return PortfolioAddDialog().apply { arguments = Bundle().apply {} }
    }
  }
}