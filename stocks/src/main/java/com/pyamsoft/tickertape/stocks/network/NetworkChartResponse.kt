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
internal data class NetworkChartResponse internal constructor(val chart: Resp) {

  @JsonClass(generateAdapter = true)
  internal data class Resp internal constructor(val result: List<Chart>) {

    @JsonClass(generateAdapter = true)
    internal data class Chart
    internal constructor(
        val meta: Meta?,
        val timestamp: List<Long>?,
        val indicators: Indicator?
    ) {

      @JsonClass(generateAdapter = true)
      internal data class Meta(
          val symbol: String?,
          val chartPreviousClose: Double?
      )

      @JsonClass(generateAdapter = true)
      internal data class Indicator(val quote: List<Quote>?) {

        @JsonClass(generateAdapter = true)
        internal data class Quote(
            val volume: List<Long?>?,
            val high: List<Double?>?,
            val low: List<Double?>?,
            val close: List<Double?>?,
            val open: List<Double?>?,
        )
      }
    }
  }
}
