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

package com.pyamsoft.tickertape.stocks

import androidx.annotation.CheckResult
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val UTC_TIME_ZONE = ZoneId.of("UTC")

private val MARKET_TIME_ZONE = ZoneId.of("US/Eastern")

/**
 * Options expiration dates are delivered in UTC time
 *
 * We map them over to a LDT and we do not care about the time, only the date. This is still an LDT
 * instead of an LD object for compatibility
 */
@CheckResult
fun parseUTCTime(
    stamp: Long,
    localZoneId: ZoneId = ZoneId.systemDefault(),
): LocalDateTime {
  return parseUTCDate(stamp, localZoneId).atTime(0, 0)
}

@CheckResult
fun parseUTCDate(
    stamp: Long,
    localZoneId: ZoneId = ZoneId.systemDefault(),
): LocalDate {
  return Instant.ofEpochSecond(stamp)
      .atZone(UTC_TIME_ZONE)
      .withZoneSameLocal(localZoneId)
      .toLocalDate()
}

@CheckResult
fun parseUTCDateTime(
    stamp: String,
    localZoneId: ZoneId = ZoneId.systemDefault(),
): LocalDateTime {
  return Instant.parse(stamp).atZone(UTC_TIME_ZONE).withZoneSameLocal(localZoneId).toLocalDateTime()
}

/**
 * Parse market related timestamps to a local time
 *
 * The market is located on the US East Coast, so we assume all timestamps are seconds in US East
 * time.
 */
@CheckResult
fun parseMarketTime(
    stamp: Long,
    localZoneId: ZoneId = ZoneId.systemDefault(),
): LocalDateTime {
  return Instant.ofEpochSecond(stamp)
      .atZone(MARKET_TIME_ZONE)
      .withZoneSameInstant(localZoneId)
      .toLocalDateTime()
}
