/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import com.pyamsoft.tickertape.worker.WorkJobType
import com.pyamsoft.tickertape.worker.WorkerQueue
import com.pyamsoft.tickertape.worker.work.bigmover.BigMoverPreferences
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class NotificationViewModeler
@Inject
internal constructor(
    override val state: MutableNotificationViewState,
    private val bigMoverPreferences: BigMoverPreferences,
    private val workerQueue: WorkerQueue,
) : NotificationViewState by state, AbstractViewModeler<NotificationViewState>(state) {

  fun bind(scope: CoroutineScope) {
    val s = state

    bigMoverPreferences.listenForBigMoverNotificationChanged().also { f ->
      scope.launch(context = Dispatchers.Default) { f.collect { s.isBigMoverEnabled.value = it } }
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
    val newEnabled = state.isBigMoverEnabled.updateAndGet { !it }
    bigMoverPreferences.setBigMoverNotificationEnabled(newEnabled)

    // Fire pref change
    scope.launch(context = Dispatchers.Default) {

      // Cancel existing alarms
      workerQueue.cancel(WorkJobType.REPEAT_BIG_MOVERS)

      // If we are enabled, fire
      if (newEnabled) {
        workerQueue.enqueue(WorkJobType.REPEAT_BIG_MOVERS)
      }
    }
  }

  companion object {
    private const val BIGMOVER_IS_ENABLED = "key_bigmover_is_enabled"
  }
}
