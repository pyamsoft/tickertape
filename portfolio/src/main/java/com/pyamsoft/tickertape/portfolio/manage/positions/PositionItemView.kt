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

package com.pyamsoft.tickertape.portfolio.manage.positions

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.db.position.DbPosition
import com.pyamsoft.tickertape.portfolio.databinding.PositionItemBinding
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.asMoney
import javax.inject.Inject

class PositionItemView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<PositionItemViewState, PositionItemViewEvent, PositionItemBinding>(parent) {

  override val viewBinding = PositionItemBinding::inflate

  override val layoutRoot by boundView { positionItem }

  init {
    doOnInflate {
      binding.positionItem.setOnLongClickListener {
        publish(PositionItemViewEvent.Remove)
        return@setOnLongClickListener true
      }
    }

    doOnTeardown { binding.positionItem.setOnLongClickListener(null) }

    doOnTeardown {
      binding.positionItemSharePrice.text = ""
      binding.positionItemNumberOfShares.text = ""
      binding.positionItemTotal.text = ""
    }
  }

  override fun onRender(state: UiRender<PositionItemViewState>) {
    state.mapChanged { it.position }.mapChanged { it.shareCount() }.render(viewScope) {
      handleShareCountChanged(it)
    }

    state.mapChanged { it.position }.mapChanged { it.price() }.render(viewScope) {
      handleSharePriceChanged(it)
    }

    state.mapChanged { it.position }.render(viewScope) { handleTotalChanged(it) }
  }

  private fun handleTotalChanged(position: DbPosition) {
    val totalValue = position.shareCount() * position.price().value()
    val total = totalValue.asMoney()
    binding.positionItemTotal.text = total.asMoneyValue()
  }

  private fun handleShareCountChanged(shareCount: Float) {
    binding.positionItemNumberOfShares.text = shareCount.toString()
  }

  private fun handleSharePriceChanged(price: StockMoneyValue) {
    binding.positionItemSharePrice.text = price.asMoneyValue()
  }
}
