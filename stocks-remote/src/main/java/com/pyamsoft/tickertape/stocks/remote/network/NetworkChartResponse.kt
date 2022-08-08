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

package com.pyamsoft.tickertape.stocks.remote.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NetworkChartResponse internal constructor(val spark: Resp) {

  @JsonClass(generateAdapter = true)
  internal data class Resp internal constructor(val result: List<SymbolChart>) {

    @JsonClass(generateAdapter = true)
    internal data class SymbolChart
    internal constructor(val symbol: String, val response: List<Chart>) {

      @JsonClass(generateAdapter = true)
      internal data class Chart
      internal constructor(
          val meta: Meta?,
          val timestamp: List<Long>?,
          val indicators: Indicator?
      ) {

        @JsonClass(generateAdapter = true)
        internal data class Meta(
            val regularMarketTime: Long?,
            val regularMarketPrice: Double?,
            val chartPreviousClose: Double?
        )

        @JsonClass(generateAdapter = true)
        internal data class Indicator(val quote: List<Quote>?) {

          @JsonClass(generateAdapter = true) internal data class Quote(val close: List<Double?>?)
        }
      }
    }
  }
}
