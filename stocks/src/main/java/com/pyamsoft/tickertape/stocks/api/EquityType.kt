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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
enum class EquityType(val display: String) {
  STOCK("Stock"),
  OPTION("Option"),
  CRYPTOCURRENCY("Crypto");

  companion object {

    private const val YF_STOCK_TYPE = "EQUITY"
    private val valueSet by lazy(LazyThreadSafetyMode.NONE) { values().toSet() }

    /**
     * Parse the EquityType string, fall back to STOCK if it is nothing else we can support
     *
     * Looping over the values is faster than using a try catch on the valueOf() static function.
     */
    @JvmStatic
    @CheckResult
    fun from(name: String): EquityType {
      // Stocks are called EQUITY by the YF API, fast track
      if (name == YF_STOCK_TYPE) {
        return STOCK
      }

      for (value in valueSet) {
        if (value.name == name) {
          return value
        }
      }

      return STOCK
    }
  }
}
