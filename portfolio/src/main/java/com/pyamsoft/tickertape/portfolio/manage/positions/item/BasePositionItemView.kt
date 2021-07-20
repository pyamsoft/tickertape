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

package com.pyamsoft.tickertape.portfolio.manage.positions.item

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.tickertape.portfolio.databinding.PositionItemBinding

abstract class BasePositionItemView<S : PositionItemViewState>
protected constructor(parent: ViewGroup) :
    BaseUiView<S, PositionItemViewEvent, PositionItemBinding>(parent) {

  final override val viewBinding = PositionItemBinding::inflate

  final override val layoutRoot by boundView { positionItem }

  init {
    doOnTeardown {
      binding.apply {
        positionItemSharePrice.text = ""
        positionItemNumberOfShares.text = ""
        positionItemTotal.text = ""
        positionItemChange.text = ""
        positionItemDate.text = ""
      }
    }
  }

  protected fun handleGainLossVisibilityChanged(isChangeVisible: Boolean) {
    binding.positionItemChange.isVisible = isChangeVisible
  }
}
