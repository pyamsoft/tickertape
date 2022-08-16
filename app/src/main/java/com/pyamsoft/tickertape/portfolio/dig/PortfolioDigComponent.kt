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

package com.pyamsoft.tickertape.portfolio.dig

import androidx.annotation.CheckResult
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.stocks.api.TradeSide
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent
internal interface PortfolioDigComponent {

  // Name arg0 because otherwise DaggerTickerComponent is bugged dagger-2.43
  fun inject(arg0: PortfolioDigFragment)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance symbol: StockSymbol,
        @BindsInstance @Named("lookup") lookupSymbol: StockSymbol?,
        @BindsInstance holdingId: DbHolding.Id,
        @BindsInstance holdingType: EquityType,
        @BindsInstance tradeSide: TradeSide,
        @BindsInstance currentPrice: StockMoneyValue?,
    ): PortfolioDigComponent
  }
}
