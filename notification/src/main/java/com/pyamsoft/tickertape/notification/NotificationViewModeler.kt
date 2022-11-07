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

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverWorkerParameters
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.alert.AlarmFactory
import com.pyamsoft.tickertape.tape.TapePreferences
import com.pyamsoft.tickertape.tape.launcher.TapeLauncher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class NotificationViewModeler
@Inject
internal constructor(
  private val state: MutableNotificationViewState,
  private val tapePreferences: TapePreferences,
  private val bigMoverPreferences: BigMoverPreferences,
  private val launcher: TapeLauncher,
  private val alarmFactory: AlarmFactory,
) : AbstractViewModeler<NotificationViewState>(state) {

  fun bind(scope: CoroutineScope) {
    val s = state

    scope.launch(context = Dispatchers.Main) {
      tapePreferences.listenForTapeNotificationChanged().collectLatest { s.isTapeEnabled = it }
    }

    scope.launch(context = Dispatchers.Main) {
      tapePreferences.listenForTapePageSizeChanged().collectLatest { s.tapePageSize = it }
    }

    scope.launch(context = Dispatchers.Main) {
      bigMoverPreferences.listenForBigMoverNotificationChanged().collectLatest {
        s.isBigMoverEnabled = it
      }
    }
  }

  override fun saveState(outState: UiSavedStateWriter) {
    val s = state

    outState.put(TAPE_IS_ENABLED, s.isTapeEnabled)
    outState.put(TAPE_PAGE_SIZE, s.tapePageSize)

    outState.put(BIGMOVER_IS_ENABLED, s.isBigMoverEnabled)
  }

  override fun restoreState(savedInstanceState: UiSavedStateReader) {
    val s = state

    savedInstanceState.get<Boolean>(TAPE_IS_ENABLED)?.also { s.isTapeEnabled = it }
    savedInstanceState.get<Int>(TAPE_PAGE_SIZE)?.also { s.tapePageSize = it }

    savedInstanceState.get<Boolean>(BIGMOVER_IS_ENABLED)?.also { s.isBigMoverEnabled = it }
  }

  fun handleTapeNotificationToggled(scope: CoroutineScope) {
    val s = state
    val newEnabled = !s.isTapeEnabled

    // Set state immediately for feedback
    state.isTapeEnabled = newEnabled

    // Fire pref change
    scope.launch(context = Dispatchers.Main) {
      tapePreferences.setTapeNotificationEnabled(newEnabled)

      // Wait for completions
      yield()

      // Restart the launcher to update if the notification fires
      launcher.start()
    }
  }

  fun handleTapePageSizeChanged(
      scope: CoroutineScope,
      size: Int,
  ) {
    // Set state immediately for feedback
    state.tapePageSize = size

    // Fire pref change
    scope.launch(context = Dispatchers.Main) { tapePreferences.setTapePageSize(size) }
  }

  fun handleBigMoverNotificationToggled(scope: CoroutineScope) {
    val s = state
    val newEnabled = !s.isBigMoverEnabled

    // Set state immediately for feedback
    state.isBigMoverEnabled = newEnabled

    // Fire pref change
    scope.launch(context = Dispatchers.Main) {
      bigMoverPreferences.setBigMoverNotificationEnabled(newEnabled)

      // Wait for completions
      yield()

      // Fire all big mover alarms in case this is going from disabled -> enabled
      alarmFactory.bigMoverAlarm(
          BigMoverWorkerParameters(
              forceRefresh = false,
          ),
      )
    }
  }

  companion object {
    private const val TAPE_IS_ENABLED = "key_tape_is_enabled"
    private const val TAPE_PAGE_SIZE = "key_tape_page_size"

    private const val BIGMOVER_IS_ENABLED = "key_bigmover_is_enabled"
  }
}
