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

package com.pyamsoft.tickertape.quote

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.quote.databinding.QuoteItemBinding
import com.pyamsoft.tickertape.quote.databinding.QuoteNumbersBinding
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class QuoteView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<QuoteViewState, QuoteViewEvent, QuoteItemBinding>(parent) {

  override val viewBinding = QuoteItemBinding::inflate

  override val layoutRoot by boundView { quoteItem }

  init {
    doOnInflate {
      binding.quoteItem.setOnLongClickListener {
        publish(QuoteViewEvent.Remove)
        return@setOnLongClickListener true
      }
    }

    doOnTeardown { binding.quoteItem.setOnLongClickListener(null) }

    doOnInflate { binding.quoteItem.setOnDebouncedClickListener { publish(QuoteViewEvent.Select) } }

    doOnTeardown { binding.quoteItem.setOnDebouncedClickListener(null) }

    doOnTeardown {
      clearBindingGroup(binding.quoteItemData.quoteItemAfterNumbers)
      clearBindingGroup(binding.quoteItemData.quoteItemNormalNumbers)

      binding.quoteItemSymbol.text = ""
      binding.quoteItemCompany.text = ""
    }
  }

  override fun onRender(state: UiRender<QuoteViewState>) {
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbolChanged(it) }

    state.mapChanged { it.data }.render(viewScope) { data ->
      when (data) {
        is QuoteViewState.QuoteData.Error -> handleDataMissing(data.error)
        is QuoteViewState.QuoteData.Quote -> handleDataPresent(data.quote)
      }
    }
  }

  private fun handleDataMissing(error: Throwable) {
    binding.quoteItemData.quoteAfterHours.isGone = true
    clearBindingGroup(binding.quoteItemData.quoteItemNormalNumbers)
    clearBindingGroup(binding.quoteItemData.quoteItemAfterNumbers)

    handleCompanyChanged(null)
    handleSessionError(error, binding.quoteItemData.quoteItemNormalNumbers)
  }

  private fun handleDataPresent(quote: StockQuote) {
    handleCompanyChanged(quote.company())
    handleRegularSessionChanged(quote.regular())
    handleAfterSessionChanged(quote.afterHours())
  }

  private fun handleRegularSessionChanged(session: StockMarketSession) {
    handleSessionChanged(session, binding.quoteItemData.quoteItemNormalNumbers)
  }

  private fun handleAfterSessionChanged(session: StockMarketSession?) {
    if (session == null) {
      binding.quoteItemData.quoteAfterHours.isGone = true
    } else {
      handleSessionChanged(session, binding.quoteItemData.quoteItemAfterNumbers)
      binding.quoteItemData.quoteAfterHours.isVisible = true
    }
  }

  private fun handleCompanyChanged(company: StockCompany?) {
    binding.quoteItemCompany.text = company?.company().orEmpty()
  }

  private fun handleSymbolChanged(symbol: StockSymbol) {
    binding.quoteItemSymbol.text = symbol.symbol()
  }

  companion object {

    @JvmStatic
    private fun handleSessionChanged(session: StockMarketSession, binding: QuoteNumbersBinding) {
      val data = StockMarketSession.getDataFromSession(session)
      val percent = data.percent
      val changeAmount = data.changeAmount
      val directionSign = data.directionSign
      val color = data.color

      binding.apply {
        quoteError.apply {
          text = ""
          isGone = true
        }

        quotePrice.apply {
          text = session.price().asMoneyValue()
          setTextColor(color)
          isVisible = true
        }

        quotePercent.apply {
          text = "(${directionSign}${percent})"
          setTextColor(color)
          isVisible = true
        }

        quoteChange.apply {
          text = "$directionSign${changeAmount}"
          setTextColor(color)
          isVisible = true
        }
      }
    }

    @JvmStatic
    private fun handleSessionError(error: Throwable, binding: QuoteNumbersBinding) {
      binding.apply {
        quoteError.apply {
          text = error.message
          setTextColor(Color.RED)
          isVisible = true
        }

        quotePrice.apply {
          text = ""
          isGone = true
        }

        quotePercent.apply {
          text = ""
          isGone = true
        }

        quoteChange.apply {
          text = ""
          isGone = true
        }
      }
    }

    @JvmStatic
    private fun clearBindingGroup(binding: QuoteNumbersBinding) {
      binding.apply {
        quoteChange.text = ""
        quotePercent.text = ""
        quotePrice.text = ""
        quoteError.text = ""
      }
    }
  }
}
