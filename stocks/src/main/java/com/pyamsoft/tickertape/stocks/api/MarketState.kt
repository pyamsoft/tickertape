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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import timber.log.Timber

@Stable
@Immutable
enum class MarketState {
  /** Normal market */
  REGULAR,

  /** After hours */
  POST,

  /** Pre-market */
  PRE;

  companion object {

    private const val PREPRE = "PREPRE"
    private const val POSTPOST = "POSTPOST"
    private const val CLOSED = "CLOSED"

    /** Convert from string to enum safely, return null instead of throwing. */
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
        // Other states from YF
        when (name) {
          PREPRE -> PRE
          POSTPOST,
          CLOSED -> POST
          else -> {
            Timber.w(e, "Unmatched MarketState: $name")
            null
          }
        }
      }
    }
  }
}
