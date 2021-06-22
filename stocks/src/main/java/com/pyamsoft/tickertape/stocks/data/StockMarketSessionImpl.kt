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

import com.pyamsoft.tickertape.stocks.api.StockDirection
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockVolumeValue

internal data class StockMarketSessionImpl(
    private val direction: StockDirection,
    private val price: StockMoneyValue,
    private val previousClosingPrice: StockMoneyValue?,
    private val amount: StockMoneyValue,
    private val percent: StockPercent,
    private val dayHigh: StockMoneyValue,
    private val dayLow: StockMoneyValue,
    private val dayOpen: StockMoneyValue,
    private val dayClose: StockMoneyValue?,
    private val dayVolume: StockVolumeValue,
) : StockMarketSession {

  override fun direction(): StockDirection {
    return direction
  }

  override fun percent(): StockPercent {
    return percent
  }

  override fun previousClosingPrice(): StockMoneyValue? {
    return previousClosingPrice
  }

  override fun price(): StockMoneyValue {
    return price
  }

  override fun amount(): StockMoneyValue {
    return amount
  }

  override fun dayVolume(): StockVolumeValue {
    return dayVolume
  }

  override fun dayOpen(): StockMoneyValue {
    return dayOpen
  }

  override fun dayLow(): StockMoneyValue {
    return dayLow
  }

  override fun dayHigh(): StockMoneyValue {
    return dayHigh
  }

  override fun dayClose(): StockMoneyValue? {
    return dayClose
  }
}
