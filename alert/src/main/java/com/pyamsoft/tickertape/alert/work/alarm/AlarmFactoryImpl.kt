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

package com.pyamsoft.tickertape.alert.work.alarm

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.alert.params.BigMoverParameters
import com.pyamsoft.tickertape.alert.params.RefreshParameters
import com.pyamsoft.tickertape.alert.preference.BigMoverPreferences
import com.pyamsoft.tickertape.alert.work.Alarm
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Singleton
internal class AlarmFactoryImpl
@Inject
internal constructor(
    private val bigMoverPreferences: BigMoverPreferences,
) : AlarmFactory {

  override suspend fun bigMoverAlarm(params: BigMoverParameters): Alarm =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext BigMoverAlarm(
            params,
            bigMoverPreferences.listenForBigMoverNotificationChanged().first(),
        )
      }

  override suspend fun refresherAlarm(params: RefreshParameters): Alarm =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext RefresherAlarm(params)
      }
}
