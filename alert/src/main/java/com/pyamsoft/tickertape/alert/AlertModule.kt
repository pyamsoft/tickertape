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

package com.pyamsoft.tickertape.alert

import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyDispatcher
import com.pyamsoft.tickertape.alert.notification.BigMoverNotificationDispatcher
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.alert.work.alarm.AlarmFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class AlertInternalApi

@Module
abstract class AlertModule {

  @Binds
  @IntoSet
  @AlertInternalApi
  internal abstract fun bindTapeDispatcher(
      impl: BigMoverNotificationDispatcher
  ): NotifyDispatcher<*>

  @Binds @CheckResult internal abstract fun bindAlarmFactory(impl: AlarmFactoryImpl): AlarmFactory

  @Module
  companion object {

    @Provides
    @JvmStatic
    @CheckResult
    @AlertInternalApi
    internal fun provideNotifier(
        // Need to use MutableSet instead of Set because of Java -> Kotlin fun.
        @AlertInternalApi dispatchers: MutableSet<NotifyDispatcher<*>>,
        context: Context
    ): Notifier {
      return Notifier.createDefault(context, dispatchers)
    }
  }
}
