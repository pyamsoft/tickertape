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

package com.pyamsoft.tickertape.portfolio.manage

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiSavedStateViewModel
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.bus.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class PortfolioSubpageViewModel<S : UiViewState, C : UiControllerEvent>
protected constructor(
    private val eventBus: EventBus<IsPortfolioSubpage>,
    savedState: UiSavedState,
    initialState: S
) : UiSavedStateViewModel<S, C>(savedState, initialState) {

  fun handleEnterSubPage() {
    viewModelScope.launch(context = Dispatchers.Default) {
      eventBus.send(IsPortfolioSubpage(isSubPage = true))
    }
  }

  fun handleExitSubPage() {
    viewModelScope.launch(context = Dispatchers.Default) {
      eventBus.send(IsPortfolioSubpage(isSubPage = false))
    }
  }
}
