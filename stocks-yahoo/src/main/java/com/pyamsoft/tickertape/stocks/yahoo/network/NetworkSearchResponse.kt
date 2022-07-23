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

package com.pyamsoft.tickertape.stocks.yahoo.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class NetworkSearchResponse
internal constructor(
    val quotes: List<Quote>,
    val news: List<News>,
) {

  @JsonClass(generateAdapter = true)
  internal data class Quote
  internal constructor(
      val symbol: String,
      val longname: String?,
      val shortname: String?,
      val score: Long,
      val quoteType: String
  ) {

    val name = shortname ?: longname
  }

  @JsonClass(generateAdapter = true)
  internal data class News
  internal constructor(
      val uuid: String,
      val title: String,
      val publisher: String,
      val link: String,
      val providerPublishTime: Long,
      val thumbnail: Thumbnail,
      val relatedTickers: List<String>,
  ) {

    @JsonClass(generateAdapter = true)
    internal data class Thumbnail
    internal constructor(
        val resolutions: List<Resolution>,
    ) {

      @JsonClass(generateAdapter = true)
      internal data class Resolution
      internal constructor(
          val url: String,
          val width: Int,
          val height: Int,
          val tag: String,
      )
    }
  }
}
