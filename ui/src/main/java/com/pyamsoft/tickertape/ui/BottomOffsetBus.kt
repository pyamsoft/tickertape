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

package com.pyamsoft.tickertape.ui

import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.core.Enforcer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BottomOffsetBus @Inject internal constructor() : EventBus<BottomOffset> {

  private val bus = EventBus.create<BottomOffset>(emitOnlyWhenActive = false, replayCount = 1)

  override suspend fun onEvent(emitter: suspend (event: BottomOffset) -> Unit) {
    Enforcer.assertOffMainThread()
    return bus.onEvent(emitter)
  }

  override suspend fun send(event: BottomOffset) {
    Enforcer.assertOffMainThread()
    bus.send(event)
  }
}
