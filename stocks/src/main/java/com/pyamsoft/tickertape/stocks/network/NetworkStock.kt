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
    internal val regularMarketPrice: Float?,
    internal val regularMarketChange: Float?,
    internal val regularMarketChangePercent: Float?,
    internal val postMarketPrice: Float?,
    internal val postMarketChange: Float?,
    internal val postMarketChangePercent: Float?,
)
