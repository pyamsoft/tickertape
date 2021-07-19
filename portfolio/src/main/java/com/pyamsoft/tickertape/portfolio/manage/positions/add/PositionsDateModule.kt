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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.tickertape.core.ActivityScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class InternalApi

@Module
abstract class PositionsDateModule {

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindBus(
      @InternalApi impl: EventBus<DateSelectPayload>
  ): EventConsumer<DateSelectPayload>

  @Module
  companion object {

    @Provides
    @JvmStatic
    @InternalApi
    @ActivityScope
    internal fun provideBus(): EventBus<DateSelectPayload> {
      return EventBus.create(emitOnlyWhenActive = false)
    }
  }
}
