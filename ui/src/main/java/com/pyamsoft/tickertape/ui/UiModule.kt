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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import dagger.Binds
import dagger.Module

@Module
abstract class UiModule {

  @Binds
  @CheckResult
  internal abstract fun bindTopOffsetBus(impl: TopOffsetBus): EventBus<TopOffset>

  @Binds
  @CheckResult
  internal abstract fun bindTopOffsetConsumer(impl: EventBus<TopOffset>): EventConsumer<TopOffset>

  @Binds
  @CheckResult
  internal abstract fun bindBottomOffsetBus(impl: BottomOffsetBus): EventBus<BottomOffset>

  @Binds
  @CheckResult
  internal abstract fun bindBottomOffsetConsumer(
      impl: EventBus<BottomOffset>
  ): EventConsumer<BottomOffset>
}
