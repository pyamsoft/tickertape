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

package com.pyamsoft.tickertape.portfolio.manage.positions.add

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.bus.EventBus
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PositionsDateViewModel
@Inject
internal constructor(
    @param:InternalApi private val dateSelectBus: EventBus<DateSelectPayload>,
) : UiViewModel<UnitViewState, PositionsDateControllerEvent>(initialState = UnitViewState) {

  fun handleDateSelected(date: LocalDateTime) {
    viewModelScope.launch(context = Dispatchers.Default) {
      dateSelectBus.send(DateSelectPayload(date))
      publish(PositionsDateControllerEvent.Close)
    }
  }
}
