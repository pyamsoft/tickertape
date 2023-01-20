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

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiSavedStateReader
import com.pyamsoft.pydroid.arch.UiSavedStateWriter
import com.pyamsoft.tickertape.alert.AlarmFactory
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverWorkerParameters
import com.pyamsoft.tickertape.alert.types.refresh.RefreshStandalone
import com.pyamsoft.tickertape.tape.TapePreferences
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class NotificationViewModeler
@Inject
internal constructor(
    override val state: MutableNotificationViewState,
    private val tapePreferences: TapePreferences,
    private val bigMoverPreferences: BigMoverPreferences,
    private val refreshStandalone: RefreshStandalone,
    private val alerter: Alerter,
    private val alarmFactory: AlarmFactory,
) : AbstractViewModeler<NotificationViewState>(state) {

  fun bind(scope: CoroutineScope) {
    val s = state

    scope.launch(context = Dispatchers.Main) {
      tapePreferences.listenForTapeNotificationChanged().collectLatest {
        s.isTapeEnabled.value = it
      }
    }

    scope.launch(context = Dispatchers.Main) {
      tapePreferences.listenForTapePageSizeChanged().collectLatest { s.tapePageSize.value = it }
    }

    scope.launch(context = Dispatchers.Main) {
      bigMoverPreferences.listenForBigMoverNotificationChanged().collectLatest {
        s.isBigMoverEnabled.value = it
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

    savedInstanceState.get<Boolean>(TAPE_IS_ENABLED)?.also { s.isTapeEnabled.value = it }
    savedInstanceState.get<Int>(TAPE_PAGE_SIZE)?.also { s.tapePageSize.value = it }
    savedInstanceState.get<Boolean>(BIGMOVER_IS_ENABLED)?.also { s.isBigMoverEnabled.value = it }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(TAPE_IS_ENABLED) { s.isTapeEnabled.value }.also { add(it) }
        registry.registerProvider(TAPE_PAGE_SIZE) { s.tapePageSize.value }.also { add(it) }
        registry
            .registerProvider(BIGMOVER_IS_ENABLED) { s.isBigMoverEnabled.value }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(TAPE_IS_ENABLED)
        ?.let { it as Boolean }
        ?.also { s.isTapeEnabled.value = it }
    registry.consumeRestored(TAPE_PAGE_SIZE)?.let { it as Int }?.also { s.tapePageSize.value = it }
    registry
        .consumeRestored(BIGMOVER_IS_ENABLED)
        ?.let { it as Boolean }
        ?.also { s.isBigMoverEnabled.value = it }
  }

  fun handleTapeNotificationToggled(scope: CoroutineScope) {
    val s = state
    val newEnabled = !s.isTapeEnabled.value

    // Set state immediately for feedback
    state.isTapeEnabled.value = newEnabled

    // Fire pref change
    scope.launch(context = Dispatchers.Main) {
      tapePreferences.setTapeNotificationEnabled(newEnabled)

      // Wait for completions
      yield()

      // Restart the launcher to update if the notification fires
      refreshStandalone.refreshTape(false)
    }
  }

  fun handleTapePageSizeChanged(
      scope: CoroutineScope,
      size: Int,
  ) {
    // Set state immediately for feedback
    state.tapePageSize.value = size

    // Fire pref change
    scope.launch(context = Dispatchers.Main) { tapePreferences.setTapePageSize(size) }
  }

  fun handleBigMoverNotificationToggled(scope: CoroutineScope) {
    val s = state
    val newEnabled = !s.isBigMoverEnabled.value

    // Set state immediately for feedback
    state.isBigMoverEnabled.value = newEnabled

    // Fire pref change
    scope.launch(context = Dispatchers.Main) {
      bigMoverPreferences.setBigMoverNotificationEnabled(newEnabled)

      // Wait for completions
      yield()

      // Fire all big mover alarms in case this is going from disabled -> enabled
      val alarm =
          alarmFactory.bigMoverAlarm(
              BigMoverWorkerParameters(
                  forceRefresh = false,
              ),
          )

      // Cancel existing alarms
      alerter.cancelAlarm(alarm)

      // If we are enabled, fire
      if (newEnabled) {
        alerter.scheduleAlarm(alarm)
      }
    }
  }

  companion object {
    private const val TAPE_IS_ENABLED = "key_tape_is_enabled"
    private const val TAPE_PAGE_SIZE = "key_tape_page_size"

    private const val BIGMOVER_IS_ENABLED = "key_bigmover_is_enabled"
  }
}
