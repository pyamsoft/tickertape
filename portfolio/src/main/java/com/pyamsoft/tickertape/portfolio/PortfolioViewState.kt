/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.tickertape.portfolio

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.db.holding.DbHolding
import com.pyamsoft.tickertape.stocks.api.EquityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@Stable
interface PortfolioViewState : UiViewState {
  val remove: StateFlow<PortfolioRemoveParams?>
  val recentlyDeleteHolding: StateFlow<DbHolding?>

  val query: StateFlow<String>
  val section: StateFlow<EquityType>
  val loadingState: StateFlow<LoadingState>

  val portfolio: StateFlow<PortfolioData?>
  val stocks: StateFlow<List<PortfolioStock>>
  val error: StateFlow<Throwable?>

  @Stable
  @Immutable
  enum class LoadingState {
    NONE,
    LOADING,
    DONE
  }
}

@Stable
class MutablePortfolioViewState @Inject internal constructor() : PortfolioViewState {
  override val remove = MutableStateFlow<PortfolioRemoveParams?>(null)
  override val recentlyDeleteHolding = MutableStateFlow<DbHolding?>(null)

  override val query = MutableStateFlow("")
  override val section = MutableStateFlow(EquityType.STOCK)
  override val loadingState = MutableStateFlow(PortfolioViewState.LoadingState.NONE)

  override val portfolio = MutableStateFlow<PortfolioData?>(null)
  override val stocks = MutableStateFlow(emptyList<PortfolioStock>())
  override val error = MutableStateFlow<Throwable?>(null)
}
