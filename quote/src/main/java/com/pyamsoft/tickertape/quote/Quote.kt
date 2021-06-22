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

import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.pyamsoft.tickertape.quote.databinding.QuoteNumbersBinding
import com.pyamsoft.tickertape.stocks.api.StockMarketSession

fun populateSession(
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

fun clearSession(binding: QuoteNumbersBinding) {
  binding.apply {
    quoteError.text = ""
    quoteChange.text = ""
    quotePercent.text = ""
    quotePrice.text = ""
  }
}
