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
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

internal data class StockNewsImpl(
    private val id: String,
    private val symbol: StockSymbol,
    private val publishedAt: LocalDateTime?,
    private val title: String,
    private val description: String,
    private val link: String,
    private val sourceName: String,
) : StockNews {

  override fun id(): String {
    return id
  }

  override fun symbol(): StockSymbol {
    return symbol
  }

  override fun publishedAt(): LocalDateTime? {
    return publishedAt
  }

  override fun title(): String {
    return title
  }

  override fun description(): String {
    return description
  }

  override fun link(): String {
    return link
  }

  override fun sourceName(): String {
    return sourceName
  }
}
