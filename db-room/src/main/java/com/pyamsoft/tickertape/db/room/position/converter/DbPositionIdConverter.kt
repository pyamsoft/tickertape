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

package com.pyamsoft.tickertape.db.room.position.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.tickertape.db.position.DbPosition

internal object DbPositionIdConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toId(id: String?): DbPosition.Id? {
    if (id == null) {
      return null
    }

    return DbPosition.Id(id)
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromId(id: DbPosition.Id?): String? {
    if (id == null) {
      return null
    }

    return id.raw
  }
}
