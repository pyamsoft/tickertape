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

package com.pyamsoft.tickertape.portfolio.item

import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.portfolio.PortfolioStock
import com.pyamsoft.tickertape.portfolio.PortfolioTabSection
import com.pyamsoft.tickertape.ui.PackedData

sealed class PortfolioItemViewState : UiViewState {
  data class Header
  internal constructor(
      val query: String,
      val section: PortfolioTabSection,
      val isLoading: Boolean,
      val portfolio: PackedData<List<PortfolioStock>>,
      val topOffset: Int,
      val bottomOffset: Int,
  ) : PortfolioItemViewState()

  data class Item internal constructor(val stock: PortfolioStock) : PortfolioItemViewState()
}

sealed class PortfolioItemViewEvent : UiViewEvent {

  object Select : PortfolioItemViewEvent()

  object Remove : PortfolioItemViewEvent()
}
