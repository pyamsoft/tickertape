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

package com.pyamsoft.tickertape.db

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.internal.DefaultEventBus
import kotlinx.coroutines.flow.Flow

internal abstract class BaseDbImpl<
    ChangeEvent : Any,
    R : DbRealtime<*>,
    Q : DbQuery<*>,
    I : DbInsert<*>,
    D : DbDelete<*>,
> protected constructor() : BaseDb<R, Q, I, D> {

  private val bus = DefaultEventBus<ChangeEvent>()

  @CheckResult protected fun subscribe(): Flow<ChangeEvent> = bus

  protected suspend fun publish(event: ChangeEvent) {
    bus.emit(event)
  }
}
