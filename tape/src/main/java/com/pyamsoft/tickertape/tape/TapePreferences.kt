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

package com.pyamsoft.tickertape.tape

import androidx.annotation.CheckResult
import kotlinx.coroutines.flow.Flow

interface TapePreferences {

  suspend fun setTapePageSize(size: Int)

  @CheckResult suspend fun listenForTapePageSizeChanged(): Flow<Int>

  suspend fun setTapeNotificationEnabled(enabled: Boolean)

  @CheckResult suspend fun listenForTapeNotificationChanged(): Flow<Boolean>

  companion object {

    const val VALUE_DEFAULT_NOTIFICATION_ENABLED = true
    const val VALUE_DEFAULT_PAGE_SIZE = 5
  }
}
