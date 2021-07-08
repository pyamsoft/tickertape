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

package com.pyamsoft.tickertape.quote.ui.view

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.quote.ui.databinding.QuoteItemBinding
import com.pyamsoft.tickertape.quote.ui.databinding.QuoteNumbersBinding
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol

abstract class QuoteView<S : UiViewState, V : UiViewEvent>
protected constructor(parent: ViewGroup) : BaseUiView<S, V, QuoteItemBinding>(parent) {

  final override val layoutRoot by boundView { quoteItem }

  final override val viewBinding = QuoteItemBinding::inflate

  init {
    doOnInflate {
      binding.quoteItem.setOnLongClickListener {
        handleRemove()
        return@setOnLongClickListener true
      }
    }

    doOnInflate { binding.quoteItem.setOnDebouncedClickListener { handleSelect() } }

    doOnTeardown {
      binding.apply {
        quoteItem.setOnLongClickListener(null)

        clearSession(quoteItemData.quoteItemAfterNumbers)
        clearSession(quoteItemData.quoteItemNormalNumbers)

        quoteItemSymbol.text = ""
        quoteItemCompany.text = ""

        quoteItem.setOnDebouncedClickListener(null)
      }
    }
  }

  protected abstract fun handleRemove()

  protected abstract fun handleSelect()

  protected fun handleRender(state: UiRender<QuoteViewState>) {
    state.mapChanged { it.symbol }.render(viewScope) { handleSymbolChanged(it) }
    state.mapChanged { it.quote }.render(viewScope) { handleQuote(it) }
  }

  private fun handleQuote(quote: StockQuote?) {
    if (quote == null) {
      handleQuoteMissing()
    } else {
      handleQuotePresent(quote)
    }
  }

  private fun handleQuoteMissing() {
    binding.quoteItemData.quoteAfterHours.isInvisible = true
    clearSession(binding.quoteItemData.quoteItemNormalNumbers)
    clearSession(binding.quoteItemData.quoteItemAfterNumbers)

    handleCompanyChanged(null)
    handleSessionError(binding.quoteItemData.quoteItemNormalNumbers)
  }

  private fun handleQuotePresent(quote: StockQuote) {
    handleCompanyChanged(quote.company())
    handleRegularSessionChanged(quote.regular())
    handleAfterSessionChanged(quote.afterHours())
  }

  private fun handleRegularSessionChanged(session: StockMarketSession) {
    populateSession(binding.quoteItemData.quoteItemNormalNumbers, session)
  }

  private fun handleAfterSessionChanged(session: StockMarketSession?) {
    if (session == null) {
      binding.quoteItemData.quoteAfterHours.isInvisible = true
    } else {
      populateSession(binding.quoteItemData.quoteItemAfterNumbers, session)
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
    private fun populateSession(
        binding: QuoteNumbersBinding,
        session: StockMarketSession,
    ) {
      val percent = session.percent().asPercentValue()
      val changeAmount = session.amount().asMoneyValue()
      val directionSign = session.direction().sign()
      val color = session.direction().color()

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
    private fun clearSession(binding: QuoteNumbersBinding) {
      binding.apply {
        quoteError.text = ""
        quoteChange.text = ""
        quotePercent.text = ""
        quotePrice.text = ""
      }
    }

    @JvmStatic
    private fun handleSessionError(binding: QuoteNumbersBinding) {
      clearSession(binding)
      binding.quoteError.apply {
        text = "Could not get quote."
        setTextColor(Color.RED)
        isVisible = true
      }
    }
  }
}
