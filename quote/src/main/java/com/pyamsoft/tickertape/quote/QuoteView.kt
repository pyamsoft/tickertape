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
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.quote.databinding.QuoteItemBinding
import com.pyamsoft.tickertape.quote.databinding.QuoteNumbersBinding
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class QuoteView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<QuoteViewState, Nothing, QuoteItemBinding>(parent) {

    override val viewBinding = QuoteItemBinding::inflate

    override val layoutRoot by boundView { quoteItem }

    init {
        doOnTeardown {
            clearBindingGroup(binding.quoteItemData.quoteItemAfterNumbers)
            clearBindingGroup(binding.quoteItemData.quoteItemNormalNumbers)

            binding.quoteItemSymbol.text = ""
            binding.quoteItemCompany.text = ""
        }
    }

    override fun onRender(state: UiRender<QuoteViewState>) {
        state.mapChanged { it.symbol }.render(viewScope) {
            handleSymbolChanged(it)
        }

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


    private fun handleCompanyChanged(company: StockCompany) {
        binding.quoteItemCompany.text = company.company()
    }

    private fun handleSymbolChanged(symbol: StockSymbol) {
        binding.quoteItemSymbol.text = symbol.symbol()
    }

    private data class SessionData(
        val percent: String,
        val changeAmount: String,
        val directionSign: String,
        @ColorInt val color: Int
    )

    companion object {

        @JvmStatic
        @CheckResult
        private fun getDataFromSession(session: StockMarketSession): SessionData {
            val percent: String
            val changeAmount: String
            val directionSign: String
            val color: Int
            if (session.direction().isZero()) {
                directionSign = ""
                color = Color.WHITE
                percent = "0"
                changeAmount = "0.00"
            } else {
                percent = session.percent().percent()
                changeAmount = session.amount().value()
                if (session.direction().isUp()) {
                    directionSign = "+"
                    color = Color.GREEN
                } else {
                    // Direction sign not needed for negative numbers
                    directionSign = ""
                    color = Color.RED
                }
            }

            return SessionData(percent, changeAmount, directionSign, color)
        }


        @JvmStatic
        private fun handleSessionChanged(
            session: StockMarketSession,
            binding: QuoteNumbersBinding
        ) {
            val data = getDataFromSession(session)
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
                    text = "\$${session.price().value()}"
                    setTextColor(color)
                    isVisible = true
                }

                quotePercent.apply {
                    text = "(${directionSign}${percent}%)"
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
