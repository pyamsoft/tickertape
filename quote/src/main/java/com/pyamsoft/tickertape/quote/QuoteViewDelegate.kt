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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.quote.databinding.QuoteItemBinding
import com.pyamsoft.tickertape.quote.databinding.QuoteNumbersBinding
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class QuoteViewDelegate @Inject internal constructor(parent: ViewGroup) {

  private var realBinding: QuoteItemBinding?
  private val binding: QuoteItemBinding
    get() = requireNotNull(realBinding)

  init {
    val inflater = LayoutInflater.from(parent.context)
    realBinding = QuoteItemBinding.inflate(inflater, parent)
  }

  @CheckResult
  fun id(): Int {
    return binding.quoteItem.id
  }

  fun inflate(onViewEvent: (QuoteViewEvent) -> Unit) {
    binding.apply {
      quoteItem.setOnLongClickListener {
        onViewEvent(QuoteViewEvent.Remove)
        return@setOnLongClickListener true
      }

      quoteItem.setOnDebouncedClickListener { onViewEvent(QuoteViewEvent.Select) }
    }
  }

  fun teardown() {
    binding.apply {
      quoteItem.setOnLongClickListener(null)

      clearSession(quoteItemData.quoteItemAfterNumbers)
      clearSession(quoteItemData.quoteItemNormalNumbers)

      quoteItemSymbol.text = ""
      quoteItemCompany.text = ""

      quoteItem.setOnDebouncedClickListener(null)
    }

    realBinding = null
  }

  fun render(scope: CoroutineScope, state: UiRender<QuoteViewState>) {
    state.mapChanged { it.symbol }.render(scope) { handleSymbolChanged(it) }

    state.mapChanged { it.quote }.render(scope) { handleQuote(it) }
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