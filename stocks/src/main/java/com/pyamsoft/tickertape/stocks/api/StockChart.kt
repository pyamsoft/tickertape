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
import java.time.LocalDateTime

interface StockChart {

  @CheckResult fun symbol(): StockSymbol

  @CheckResult fun range(): IntervalRange

  @CheckResult fun interval(): IntervalTime

  @CheckResult fun dates(): List<LocalDateTime>

  @CheckResult fun volume(): List<StockVolumeValue>

  @CheckResult fun open(): List<StockMoneyValue>

  @CheckResult fun close(): List<StockMoneyValue>

  @CheckResult fun high(): List<StockMoneyValue>

  @CheckResult fun low(): List<StockMoneyValue>

  enum class IntervalTime(val apiValue: String) {
    ONE_MINUTE("1m"),
    TWO_MINUTES("2m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    THIRY_MINUTES("30m"),
    SIXTY_MINUTES("60m"),
    NINETY_MINUTES("90m"),
    ONE_HOUR("1h"),
    ONE_DAY("1d"),
    FIVE_DAYS("5d"),
    ONE_WEEK("1wk"),
    ONE_MONTH("1mo"),
    THREE_MONTH("3mo")
  }

  enum class IntervalRange(val apiValue: String) {
    ONE_DAY("1d"),
    FIVE_DAY("5d"),
    ONE_MONTH("1mo"),
    THREE_MONTH("3mo"),
    SIX_MONTH("6mo"),
    ONE_YEAR("1y"),
    TWO_YEAR("2y"),
    FIVE_YEAR("5y"),
    TEN_YEAR("10y"),
    YTD("ytd"),
    MAX("max")
  }
}
