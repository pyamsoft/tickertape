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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.tickertape.portfolio.databinding.PositionItemBinding
import javax.inject.Inject

class PositionHeaderView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PositionItemViewState.Header, PositionItemViewEvent, PositionItemBinding>(parent) {

  override val viewBinding = PositionItemBinding::inflate

  override val layoutRoot by boundView { positionItem }

  init {
    doOnTeardown {
      binding.apply {
        positionItemSharePrice.text = ""
        positionItemNumberOfShares.text = ""
        positionItemTotal.text = ""
      }
    }

    doOnInflate {
      binding.apply {
        positionItemSharePrice.text = "Price"
        positionItemNumberOfShares.text = "Shares"
        positionItemTotal.text = "Total"
      }
    }
  }
}
