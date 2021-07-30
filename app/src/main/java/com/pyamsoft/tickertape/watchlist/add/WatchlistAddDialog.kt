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

package com.pyamsoft.tickertape.watchlist.add

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.arch.createSavedStateViewModelFactory
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.stocks.api.HoldingType
import com.pyamsoft.tickertape.stocks.api.toHoldingString
import com.pyamsoft.tickertape.symbol.SymbolAddDialog
import javax.inject.Inject

internal class WatchlistAddDialog : SymbolAddDialog<WatchlistAddViewModel>() {

  @JvmField @Inject internal var factory: WatchlistAddViewModel.Factory? = null
  override val viewModel by fromViewModelFactory<WatchlistAddViewModel> {
    createSavedStateViewModelFactory(factory)
  }

  override fun onInject(view: ViewGroup, savedInstanceState: Bundle?, type: HoldingType) {
    Injector.obtainFromApplication<TickerComponent>(view.context)
        .plusWatchlistAddComponent()
        .create(
            this,
            requireActivity(),
            viewLifecycleOwner,
        )
        .plusWatchlistAddComponent()
        .create(view, type)
        .inject(this)
  }

  override fun onTeardown() {
    factory = null
  }

  companion object {

    const val TAG = "WatchlistAddDialog"

    @JvmStatic
    @CheckResult
    fun newInstance(type: HoldingType): DialogFragment {
      return WatchlistAddDialog().apply {
        arguments = Bundle().apply { putString(KEY_HOLDING_TYPE, type.toHoldingString()) }
      }
    }
  }
}
