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

import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@JvmField
val SHARES_FORMATTER =
    object : ThreadLocal<NumberFormat>() {

      override fun initialValue(): NumberFormat? {
        return DecimalFormat.getNumberInstance().apply {
          minimumFractionDigits = 0
          maximumFractionDigits = 6
        }
      }
    }

@JvmField
val SMALL_MONEY_FORMATTER =
    object : ThreadLocal<NumberFormat>() {

      override fun initialValue(): NumberFormat? {
        return DecimalFormat.getCurrencyInstance().apply {
          minimumFractionDigits = 3
          maximumFractionDigits = 3
        }
      }
    }

@JvmField
val BIG_MONEY_FORMATTER =
    object : ThreadLocal<NumberFormat>() {

      override fun initialValue(): NumberFormat? {
        return DecimalFormat.getCurrencyInstance().apply {
          minimumFractionDigits = 2
          maximumFractionDigits = 2
        }
      }
    }

@JvmField
val DATE_TIME_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
      }
    }

@JvmField
val DATE_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
      }
    }

@JvmField
val PERCENT_FORMATTER =
    object : ThreadLocal<NumberFormat>() {

      override fun initialValue(): NumberFormat {
        return DecimalFormat.getPercentInstance().apply {
          minimumFractionDigits = 2
          maximumFractionDigits = 2
        }
      }
    }

@JvmField
val VOLUME_FORMATTER =
    object : ThreadLocal<NumberFormat>() {

      override fun initialValue(): NumberFormat {
        return DecimalFormat.getNumberInstance().apply {
          isParseIntegerOnly = true
          minimumFractionDigits = 0
          maximumFractionDigits = 0
        }
      }
    }
