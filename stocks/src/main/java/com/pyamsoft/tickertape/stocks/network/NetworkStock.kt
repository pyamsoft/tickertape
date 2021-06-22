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

package com.pyamsoft.tickertape.stocks.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NetworkStock
internal constructor(
    internal val symbol: String,
    internal val shortName: String?,
    internal val exchangeDataDelayedBy: Long?,
    // Regular market
    internal val regularMarketPrice: Double?,
    internal val regularMarketChange: Double?,
    internal val regularMarketChangePercent: Double?,
    internal val regularMarketPreviousClose: Double?,
    internal val regularMarketOpen: Double?,
    internal val regularMarketClose: Double?,
    internal val regularMarketDayHigh: Double?,
    internal val regularMarketDayLow: Double?,
    internal val regularMarketDayRange: String?,
    internal val regularMarketVolume: Long?,
    // Post market
    internal val postMarketPrice: Double?,
    internal val postMarketChange: Double?,
    internal val postMarketChangePercent: Double?,
    internal val postMarketPreviousClose: Double?,
    internal val postMarketOpen: Double?,
    internal val postMarketClose: Double?,
    internal val postMarketDayHigh: Double?,
    internal val postMarketDayLow: Double?,
    internal val postMarketDayRange: String?,
    internal val postMarketVolume: Long?,
)
