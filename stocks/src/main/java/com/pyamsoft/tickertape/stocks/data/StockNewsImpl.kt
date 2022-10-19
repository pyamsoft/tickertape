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

package com.pyamsoft.tickertape.stocks.data

import com.pyamsoft.tickertape.stocks.api.StockNews
import com.pyamsoft.tickertape.stocks.api.StockNewsList
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

internal data class StockNewsImpl(
    override val id: String,
    override val symbol: StockSymbol,
    override val publishedAt: LocalDateTime?,
    override val title: String,
    override val description: String,
    override val link: String,
    override val sourceName: String,
    override val imageUrl: String,
    override val tickers: List<StockSymbol>,
) : StockNews

internal data class StockNewsListImpl(
    override val symbol: StockSymbol,
    override val news: List<StockNews>
) : StockNewsList {

  override fun <R : Comparable<R>> sortedByDescending(selector: (StockNews) -> R?): StockNewsList {
    return this.copy(
        news = news.sortedByDescending(selector),
    )
  }
}
