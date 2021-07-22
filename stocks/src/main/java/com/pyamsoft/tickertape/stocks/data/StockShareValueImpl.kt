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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.core.isZero
import com.pyamsoft.tickertape.stocks.api.StockShareValue

internal data class StockShareValueImpl(private val value: Double) : StockShareValue {

  private val share by lazy(LazyThreadSafetyMode.NONE) {
    if (isZero()) {
      return@lazy "0"
    }

    // Parse to int to remove the decimals, then back to float for comparison ability
    val intValue = value.toInt()
    val valueWithoutDecimal = intValue.toDouble()
    if (valueWithoutDecimal.compareTo(value) == 0) {
      // This is a number without any decimals, return the int value as a String
      return@lazy intValue.toString()
    } else {
      return@lazy value.toString()
    }
  }

  override fun asShareValue(): String {
    return share
  }

  override fun value(): Double {
    return value
  }

  override fun isZero(): Boolean {
    return value.isZero()
  }
}
