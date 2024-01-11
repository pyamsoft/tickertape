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
import com.pyamsoft.tickertape.stocks.data.StockTopsImpl

interface StockTops {

  @get:CheckResult val title: String

  @get:CheckResult val description: String

  @get:CheckResult val quotes: List<StockQuote>

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        title: String,
        description: String,
        quotes: List<StockQuote>,
    ): StockTops {
      return StockTopsImpl(
          title,
          description,
          quotes,
      )
    }
  }
}
