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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult

sealed class HoldingType {
  object Stock : HoldingType()

  sealed class Options : HoldingType() {

    object Buy : Options()

    object Sell : Options()
  }
}

@CheckResult
fun HoldingType.isOption(): Boolean {
  return this != HoldingType.Stock
}

private const val HOLDING_TYPE_STOCK = "holding_stock"
private const val HOLDING_TYPE_OPTION_BUY = "holding_option_buy"
private const val HOLDING_TYPE_OPTION_SELL = "holding_option_sell"

@CheckResult
fun HoldingType.toHoldingString(): String {
  return when (this) {
    is HoldingType.Options.Buy -> HOLDING_TYPE_OPTION_BUY
    is HoldingType.Options.Sell -> HOLDING_TYPE_OPTION_SELL
    is HoldingType.Stock -> HOLDING_TYPE_STOCK
  }
}

@CheckResult
fun String.fromHoldingString(): HoldingType {
  return when (this) {
    HOLDING_TYPE_OPTION_BUY -> HoldingType.Options.Buy
    HOLDING_TYPE_OPTION_SELL -> HoldingType.Options.Sell
    HOLDING_TYPE_STOCK -> HoldingType.Stock
    else -> throw IllegalArgumentException("String cannot be converted to HoldingType $this")
  }
}
