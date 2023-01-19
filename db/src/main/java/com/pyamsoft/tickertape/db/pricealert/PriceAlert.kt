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

package com.pyamsoft.tickertape.db.pricealert

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.tickertape.db.IdType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import java.time.LocalDateTime

@Stable
interface PriceAlert {

  @get:CheckResult val id: Id

  @get:CheckResult val symbol: StockSymbol

  @get:CheckResult val lastNotified: LocalDateTime?

  @get:CheckResult val triggerPriceAbove: StockMoneyValue?

  @get:CheckResult val triggerPriceBelow: StockMoneyValue?

  @get:CheckResult val enabled: Boolean

  @CheckResult fun lastNotified(notified: LocalDateTime): PriceAlert

  @CheckResult fun watchForPriceAbove(price: StockMoneyValue): PriceAlert

  @CheckResult fun clearPriceAbove(): PriceAlert

  @CheckResult fun watchForPriceBelow(price: StockMoneyValue): PriceAlert

  @CheckResult fun clearPriceBelow(): PriceAlert

  @CheckResult fun enable(): PriceAlert

  @CheckResult fun disable(): PriceAlert

  @Stable
  data class Id(override val raw: String) : IdType {

    override val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }
}
