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
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.bus.EventConsumer
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagePortfolioViewModel
@Inject
internal constructor(eventConsumer: EventConsumer<IsPortfolioSubpage>) :
    UiViewModel<ManagePortfolioViewState, ManagePortfolioControllerEvent>(
        initialState = ManagePortfolioViewState(isClose = true)) {

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      eventConsumer.onEvent { event -> setState { copy(isClose = !event.isSubPage) } }
    }
  }

  fun handleLoadDefaultPage() {
    setState(
        stateChange = { copy(isClose = true) },
        andThen = { publish(ManagePortfolioControllerEvent.PushHoldingFragment) })
  }
}
