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

package com.pyamsoft.tickertape.db.room.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.tickertape.stocks.api.HoldingType

internal object HoldingTypeConverter {

  private const val HOLDING_TYPE_EQUITY = "EQUITY"
  private const val HOLDING_TYPE_OPTION_BUY = "OPTION_BUY"
  private const val HOLDING_TYPE_OPTION_SELL = "OPTION_SELL"

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toType(type: String): HoldingType {
    return when (type) {
      HOLDING_TYPE_EQUITY -> HoldingType.Equity
      HOLDING_TYPE_OPTION_BUY -> HoldingType.Options.Buy
      HOLDING_TYPE_OPTION_SELL -> HoldingType.Options.Sell
      else ->
          throw AssertionError(
              "Unexpected HoldingType: $type. Expected one of [$HOLDING_TYPE_EQUITY $HOLDING_TYPE_OPTION_BUY $HOLDING_TYPE_OPTION_SELL]")
    }
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromType(type: HoldingType): String {
    return when (type) {
      is HoldingType.Equity -> HOLDING_TYPE_EQUITY
      is HoldingType.Options.Buy -> HOLDING_TYPE_OPTION_BUY
      is HoldingType.Options.Sell -> HOLDING_TYPE_OPTION_SELL
    }
  }
}
