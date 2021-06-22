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

interface StockMarketSession {

  @CheckResult fun direction(): StockDirection

  @CheckResult fun price(): StockMoneyValue

  @CheckResult fun previousClosingPrice(): StockMoneyValue?

  @CheckResult fun amount(): StockMoneyValue

  @CheckResult fun percent(): StockPercent

  @CheckResult fun dayOpen(): StockMoneyValue

  @CheckResult fun dayClose(): StockMoneyValue?

  @CheckResult fun dayHigh(): StockMoneyValue

  @CheckResult fun dayLow(): StockMoneyValue

  @CheckResult fun dayVolume(): StockVolumeValue
}
