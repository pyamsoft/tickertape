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

package com.pyamsoft.tickertape.notification

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UnitControllerEvent
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.params.BigMoverParameters
import com.pyamsoft.tickertape.alert.preference.BigMoverPreferences
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.tape.TapeLauncher
import com.pyamsoft.tickertape.tape.TapePreferences
import com.pyamsoft.tickertape.ui.BottomOffset
import com.pyamsoft.tickertape.ui.TopOffset
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationViewModel
@Inject
internal constructor(
    private val tapePreferences: TapePreferences,
    private val bigMoverPreferences: BigMoverPreferences,
    private val tapeLauncher: TapeLauncher,
    private val alarmFactory: AlarmFactory,
    private val alerter: Alerter,
    bottomOffsetBus: EventConsumer<BottomOffset>,
    topOffsetBus: EventConsumer<TopOffset>,
) :
    UiViewModel<NotificationViewState, UnitControllerEvent>(
        initialState =
            NotificationViewState(
                topOffset = 0,
                bottomOffset = 0,
                isTapeEnabled = false,
                isBigMoverEnabled = false,
            ),
    ) {

  init {
    viewModelScope.launch(context = Dispatchers.Default) {
      bottomOffsetBus.onEvent { setState { copy(bottomOffset = it.height) } }
    }

    viewModelScope.launch(context = Dispatchers.Default) {
      topOffsetBus.onEvent { setState { copy(topOffset = it.height) } }
    }

    setState(
        stateChange = { copy(isTapeEnabled = tapePreferences.isTapeNotificationEnabled()) },
        andThen = { updateTapeLauncher() })

    viewModelScope.launch(context = Dispatchers.Default) {
      tapePreferences
          .listenForTapeNotificationChanged { enabled ->
            // Need coroutine scope here so we can use the PreferenceListener.scope
            // instead of the viewModelScope.launch declared above
            //
            // If we use the scope launch declared above, it will be dead by the time we get here
            // so setState will not actually run.
            coroutineScope {
              setState(
                  stateChange = { copy(isTapeEnabled = enabled) },
                  andThen = { updateTapeLauncher() })
            }
          }
          .also {
            // Must be on main thread
            withContext(context = Dispatchers.Main) { doOnCleared { it.cancel() } }
          }
    }

    setState(
        stateChange = {
          copy(isBigMoverEnabled = bigMoverPreferences.isBigMoverNotificationEnabled())
        },
        andThen = { updateBigMoverAlerts() })

    viewModelScope.launch(context = Dispatchers.Default) {
      bigMoverPreferences
          .listenForBigMoverNotificationChanged { enabled ->
            // Need coroutine scope here so we can use the PreferenceListener.scope
            // instead of the viewModelScope.launch declared above
            //
            // If we use the scope launch declared above, it will be dead by the time we get here
            // so setState will not actually run.
            coroutineScope {
              setState(
                  stateChange = { copy(isBigMoverEnabled = enabled) },
                  andThen = { updateBigMoverAlerts() })
            }
          }
          .also {
            // Must be on main thread
            withContext(context = Dispatchers.Main) { doOnCleared { it.cancel() } }
          }
    }
  }

  private suspend fun updateBigMoverAlerts() {
    val alarm = alarmFactory.bigMoverAlarm(BigMoverParameters(forceRefresh = false))
    alerter.scheduleAlarm(alarm)
  }

  private suspend fun updateTapeLauncher() {
    tapeLauncher.start()
  }

  fun handleTapeUpdated(enabled: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) {
      tapePreferences.setTapeNotificationEnabled(enabled)
    }
  }

  fun handleBigMoverUpdated(enabled: Boolean) {
    viewModelScope.launch(context = Dispatchers.Default) {
      bigMoverPreferences.setBigMoverNotificationEnabled(enabled)
    }
  }
}
