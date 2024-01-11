/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.db.room.mover.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.asPercent

internal object StockPercentConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toPercent(percent: Double?): StockPercent? {
    if (percent == null) {
      return null
    }

    return percent.asPercent()
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromPercent(percent: StockPercent?): Double? {
    if (percent == null) {
      return null
    }

    return percent.value
  }
}
