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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.stocks.data.StockNewsImpl
import com.pyamsoft.tickertape.stocks.data.StockNewsListImpl
import java.time.LocalDateTime

@Stable
interface StockNewsList {

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val news: List<StockNews>

  @CheckResult
  fun <R : Comparable<R>> sortedByDescending(
      selector: (StockNews) -> R?,
  ): StockNewsList

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        symbol: StockSymbol,
        news: List<StockNews>,
    ): StockNewsList {
      return StockNewsListImpl(
          symbol = symbol,
          news = news,
      )
    }
  }
}

@Stable
interface StockNews {

  @get:CheckResult val id: String

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val publishedAt: LocalDateTime?

  @get:CheckResult val title: String

  @get:CheckResult val description: String

  @get:CheckResult val link: String

  @get:CheckResult val sourceName: String

  @get:CheckResult val imageUrl: String

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        id: String,
        symbol: StockSymbol,
        publishedAt: LocalDateTime?,
        title: String,
        description: String,
        link: String,
        sourceName: String,
        imageUrl: String,
    ): StockNews {
      return StockNewsImpl(
          id = id,
          symbol = symbol,
          publishedAt = publishedAt,
          title = title,
          description = description,
          link = link,
          sourceName = sourceName,
          imageUrl = imageUrl,
      )
    }
  }
}
