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
internal data class NetworkOptionResponse internal constructor(val optionChain: Resp) {

  @JsonClass(generateAdapter = true)
  internal data class Resp internal constructor(val result: List<OptionChain>) {

    @JsonClass(generateAdapter = true)
    internal data class OptionChain
    internal constructor(
        val underlyingSymbol: String,
        val expirationDates: List<Long>,
        val strikes: List<Double>,
        val quote: NetworkQuoteResponse.Resp.Quote,
        val options: List<Option>
    ) {

      @JsonClass(generateAdapter = true)
      internal data class Option
      internal constructor(
          val expirationDate: Long,
          val calls: List<OptionContract>,
          val puts: List<OptionContract>
      ) {

        @JsonClass(generateAdapter = true)
        internal data class OptionContract
        internal constructor(
            val contractSymbol: String,
            val strike: Double,
            val lastPrice: Double,
            val percentChange: Double,
            val impliedVolatility: Double,
            val inTheMoney: Boolean,
            val change: Double,
            val lastTradeDate: Long,
            val expiration: Long,
            val bid: Double?,
            val ask: Double?,
            val openInterest: Int?,
        )
      }
    }
  }
}
