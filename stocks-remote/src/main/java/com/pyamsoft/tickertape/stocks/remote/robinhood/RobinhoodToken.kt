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

package com.pyamsoft.tickertape.stocks.remote.robinhood

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

internal data class RobinhoodToken
internal constructor(
    val accessToken: String,
    val expiresInMilliseconds: Long,
) {

  val expiresAt: LocalDateTime =
      LocalDateTime.now()
          // Figure out the expire time
          .plusNanos(TimeUnit.MILLISECONDS.toNanos(expiresInMilliseconds))
          // And then adjust just to be safe
          .minusSeconds(10)
}
