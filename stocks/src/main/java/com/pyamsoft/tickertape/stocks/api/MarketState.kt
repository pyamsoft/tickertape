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
import timber.log.Timber

enum class MarketState {
  REGULAR,
  POST,
  PRE;

  companion object {

    /**
     * Convert from string to enum safely, return null instead of throwing.
     */
    @JvmStatic
    @CheckResult
    fun from(name: String?): MarketState? {
      if (name == null) {
        Timber.w("Cannot convert null state to MarketState")
        return null
      }

      return try {
        valueOf(name)
      } catch (e: Throwable) {
        Timber.e(e, "Unmatched MarketState: $name")
        null
      }
    }
  }
}