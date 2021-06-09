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
import com.pyamsoft.tickertape.stocks.api.StockCompany
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import javax.inject.Inject

class QuoteView @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<QuoteViewState, Nothing, QuoteItemBinding>(parent) {

  override val viewBinding = QuoteItemBinding::inflate

  override val layoutRoot by boundView { quoteItem }

  init {
    doOnTeardown {
      binding.quoteItemData.quoteItemAfterNumbers.apply {
        quoteChange.text = ""
        quotePercent.text = ""
        quotePrice.text = ""
      }

      binding.quoteItemData.quoteItemNormalNumbers.apply {
        quoteChange.text = ""
        quotePercent.text = ""
        quotePrice.text = ""
      }

      binding.quoteItemSymbol.text = ""
      binding.quoteItemCompany.text = ""
    }
  }

  override fun onRender(state: UiRender<QuoteViewState>) {
    state.mapChanged { it.quote }.mapChanged { it.symbol() }.render(viewScope) {
      handleSymbolChanged(it)
    }

    state.mapChanged { it.quote }.mapChanged { it.company() }.render(viewScope) {
      handleCompanyChanged(it)
    }

    state.mapChanged { it.quote }.mapChanged { it.regular() }.render(viewScope) {
      handleRegularSessionChanged(it)
    }

    state.mapChanged { it.quote }.mapChanged { it.afterHours() }.render(viewScope) {
      handleAfterSessionChanged(it)
    }
  }

  private fun handleAfterSessionChanged(session: StockMarketSession?) {
    if (session == null) {
      binding.quoteItemData.quoteAfterHours.isGone = true
    } else {
      val data = getDataFromSession(session)
      val percent = data.percent
      val changeAmount = data.changeAmount
      val directionSign = data.directionSign
      val color = data.color

      binding.quoteItemData.quoteItemAfterNumbers.apply {
        quotePrice.apply {
          text = "\$${session.price().value()}"
          setTextColor(color)
        }
        quotePercent.apply {
          text = "(${directionSign}${percent}%)"
          setTextColor(color)
        }
        quoteChange.apply {
          text = "$directionSign${changeAmount}"
          setTextColor(color)
        }
      }

      binding.quoteItemData.quoteAfterHours.isVisible = true
    }
  }

  private fun handleRegularSessionChanged(session: StockMarketSession) {
    val data = getDataFromSession(session)
    val percent = data.percent
    val changeAmount = data.changeAmount
    val directionSign = data.directionSign
    val color = data.color

    binding.quoteItemData.quoteItemNormalNumbers.apply {
      quotePrice.apply {
        text = "\$${session.price().value()}"
        setTextColor(color)
      }
      quotePercent.apply {
        text = "(${directionSign}${percent}%)"
        setTextColor(color)
      }
      quoteChange.apply {
        text = "$directionSign${changeAmount}"
        setTextColor(color)
      }
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
  }
}
