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

package com.pyamsoft.tickertape.quote.add

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tickertape.stocks.api.EquityType
import javax.inject.Inject

class NewTickerViewModeler
@Inject
internal constructor(
    private val state: MutableNewTickerViewState,
) : AbstractViewModeler<NewTickerViewState>(state) {

  fun handleSymbolChanged(symbol: String) {
    state.symbol = symbol
  }

  fun handleEquityTypeSelected(equityType: EquityType) {
    state.equityType = equityType
  }

  fun handleClearEquityType() {
    state.equityType = null
  }
}
