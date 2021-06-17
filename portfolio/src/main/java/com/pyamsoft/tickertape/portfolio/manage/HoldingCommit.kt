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

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.portfolio.databinding.HoldingCommitBinding
import javax.inject.Inject

class HoldingCommit @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HoldingViewState, HoldingViewEvent, HoldingCommitBinding>(parent) {

  override val viewBinding = HoldingCommitBinding::inflate

  override val layoutRoot by boundView { positionCommitRoot }

  init {
    doOnInflate {
      binding.positionCommit.setOnDebouncedClickListener { publish(HoldingViewEvent.Commit) }
    }

    doOnInflate {
      binding.positionCommit.setOnLongClickListener {
        publish(HoldingViewEvent.ListPositions)
        return@setOnLongClickListener true
      }
    }

    doOnTeardown { binding.positionCommit.setOnLongClickListener(null) }

    doOnTeardown { binding.positionCommit.setOnDebouncedClickListener(null) }
  }

  override fun onRender(state: UiRender<HoldingViewState>) {
    state.render(viewScope) { handleStateChanged(it) }
  }

  private fun handleStateChanged(state: HoldingViewState) {
    val sharePrice = state.pricePerShare
    val shareCount = state.numberOfShares
    val isEntered = shareCount.value().compareTo(0) > 0 && sharePrice.value().compareTo(0) > 0
    binding.positionCommit.isEnabled = isEntered
  }
}
