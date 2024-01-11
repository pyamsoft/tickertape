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

package com.pyamsoft.tickertape.db.holding

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.IdType
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide

@Stable
interface DbHolding {

  @get:CheckResult val id: Id

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val type: EquityType

  @get:CheckResult val side: TradeSide

  data class Id(override val raw: String) : IdType {

    override val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }
}
