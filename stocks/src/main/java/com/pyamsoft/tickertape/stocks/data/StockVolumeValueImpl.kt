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

import com.pyamsoft.tickertape.stocks.api.StockVolumeValue
import java.text.DecimalFormat
import java.text.NumberFormat

internal data class StockVolumeValueImpl(private val value: Long) : StockVolumeValue {

  private val volume by lazy(LazyThreadSafetyMode.NONE) {
    if (isZero()) "0" else requireNotNull(FORMATTER.get()).format(value)
  }

  override fun asVolumeValue(): String {
    return volume
  }

  override fun value(): Long {
    return value
  }

  override fun isZero(): Boolean {
    return value.compareTo(0) == 0
  }

  companion object {
    private val FORMATTER =
        object : ThreadLocal<NumberFormat>() {

          override fun initialValue(): NumberFormat {
            return DecimalFormat.getNumberInstance().apply {
              isParseIntegerOnly = true
              minimumFractionDigits = 0
              maximumFractionDigits = 0
            }
          }
        }
  }
}
