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

package com.pyamsoft.tickertape.db.room.holding.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.tickertape.stocks.api.TradeSide

internal object TradeSideConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toSide(side: String?): TradeSide? {
    if (side == null) {
      return null
    }

    return TradeSide.valueOf(side)
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromSide(side: TradeSide?): String? {
    if (side == null) {
      return null
    }

    return side.name
  }
}
