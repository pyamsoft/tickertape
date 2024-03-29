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

package com.pyamsoft.tickertape.stocks.remote.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NetworkTopResponse internal constructor(val finance: Resp) {

  @JsonClass(generateAdapter = true)
  internal data class Resp internal constructor(val result: List<Top>) {

    @JsonClass(generateAdapter = true)
    internal data class Top
    internal constructor(
        val id: String?,
        val title: String?,
        val description: String?,
        val quotes: List<NetworkQuoteResponse.Resp.Quote>?
    )
  }
}
