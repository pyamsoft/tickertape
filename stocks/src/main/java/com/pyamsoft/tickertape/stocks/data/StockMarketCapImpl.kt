/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.BIG_MONEY_FORMATTER
import com.pyamsoft.tickertape.stocks.api.StockMarketCap
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal data class StockMarketCapImpl(
    override val value: Long,
    override val isValid: Boolean,
) : StockMarketCap {

  private val volume by
      lazy(LazyThreadSafetyMode.NONE) {
        if (isZero) "0"
        else {
          // Treat long as big so that 17000000000 becomes 1.70T
          val marketCapBig = value.toBigDecimal()
          val formatter = BIG_MONEY_FORMATTER.get().requireNotNull()

          val marketCapFinal: Double
          val marketCapUnit: String

          if (marketCapBig >= BIG_DECIMAL_QUADRILLION) {
            marketCapFinal =
                marketCapBig.divide(BIG_DECIMAL_QUADRILLION, DIVISION_CONTEXT).toDouble()
            marketCapUnit = "Q"
          } else if (marketCapBig >= BIG_DECIMAL_TRILLION) {
            marketCapFinal = marketCapBig.divide(BIG_DECIMAL_TRILLION, DIVISION_CONTEXT).toDouble()
            marketCapUnit = "T"
          } else if (marketCapBig >= BIG_DECIMAL_BILLION) {
            marketCapFinal = marketCapBig.divide(BIG_DECIMAL_BILLION, DIVISION_CONTEXT).toDouble()
            marketCapUnit = "B"
          } else if (marketCapBig >= BIG_DECIMAL_MILLION) {
            marketCapFinal = marketCapBig.divide(BIG_DECIMAL_MILLION, DIVISION_CONTEXT).toDouble()
            marketCapUnit = "M"
          } else {
            marketCapFinal = marketCapBig.divide(BIG_DECIMAL_K, DIVISION_CONTEXT).toDouble()
            marketCapUnit = "K"
          }

          // substring(1) removes the $ currency symbol
          // NOTE(Peter): Will this be fucked up with locale currency being different from USD? Oh
          // well
          // for now, we only support USD.
          val cleanNumberNoDollarSign = formatter.format(marketCapFinal).substring(1)

          // Format string
          "${cleanNumberNoDollarSign}${marketCapUnit}"
        }
      }

  override val isZero: Boolean = value.isZero()

  override val display: String = volume

  companion object {

    private val DIVISION_CONTEXT = MathContext(4, RoundingMode.HALF_UP)

    private val BIG_DECIMAL_K = BigDecimal(1_000L)
    private val BIG_DECIMAL_MILLION = BigDecimal(1_000_000L)
    private val BIG_DECIMAL_BILLION = BigDecimal(1_000_000_000L)
    private val BIG_DECIMAL_TRILLION = BigDecimal(1_000_000_000_000L)
    private val BIG_DECIMAL_QUADRILLION = BigDecimal(1_000_000_000_000_000L)
  }
}
