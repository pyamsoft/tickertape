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

package com.pyamsoft.tickertape.notification

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tickertape.alert.AlarmFactory
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverWorkerParameters
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
    private val bigMoverPreferences: BigMoverPreferences,
    private val alerter: Alerter,
    private val alarmFactory: AlarmFactory,
) : AbstractViewModeler<NotificationViewState>(state) {

  fun bind(scope: CoroutineScope) {
    val s = state

    scope.launch(context = Dispatchers.Main) {
      bigMoverPreferences.listenForBigMoverNotificationChanged().collectLatest {
        s.isBigMoverEnabled.value = it
      }
    }
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry
            .registerProvider(BIGMOVER_IS_ENABLED) { s.isBigMoverEnabled.value }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(BIGMOVER_IS_ENABLED)
        ?.let { it as Boolean }
        ?.also { s.isBigMoverEnabled.value = it }
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
    private const val BIGMOVER_IS_ENABLED = "key_bigmover_is_enabled"
  }
}
