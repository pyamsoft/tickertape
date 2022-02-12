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

package com.pyamsoft.tickertape.stocks.api

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.stocks.data.StockNewsImpl
import java.time.LocalDateTime

interface StockNews {

  @CheckResult fun id(): String

  @CheckResult fun symbol(): StockSymbol

  @CheckResult fun publishedAt(): LocalDateTime?

  @CheckResult fun title(): String

  @CheckResult fun description(): String

  @CheckResult fun link(): String

  @CheckResult fun sourceName(): String

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
    ): StockNews {
      return StockNewsImpl(
          id = id,
          symbol = symbol,
          publishedAt = publishedAt,
          title = title,
          description = description,
          link = link,
          sourceName = sourceName,
      )
    }
  }
}
