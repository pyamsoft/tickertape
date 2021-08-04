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

package com.pyamsoft.tickertape.stocks.sources.yf

import androidx.annotation.CheckResult
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val MARKET_TIME_ZONE = ZoneId.of("US/Eastern")

/**
 * Parse market related timestamps to a local time
 *
 * The market is located on the US East Coast, so we assume all timestamps are seconds in US East
 * time.
 */
@CheckResult
internal fun parseMarketTime(stamp: Long, localZoneId: ZoneId): LocalDateTime {
  return ZonedDateTime.ofInstant(Instant.ofEpochSecond(stamp), MARKET_TIME_ZONE)
      .withZoneSameInstant(localZoneId)
      .toLocalDateTime()
}
