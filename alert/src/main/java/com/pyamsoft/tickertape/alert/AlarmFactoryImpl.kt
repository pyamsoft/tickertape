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

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.alert.base.Alarm
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverAlarm
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverWorkerParameters
import com.pyamsoft.tickertape.alert.types.refresh.RefreshWorkerParameters
import com.pyamsoft.tickertape.alert.types.refresh.RefresherAlarm
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

  override suspend fun bigMoverAlarm(params: BigMoverWorkerParameters): Alarm =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val isEnabled = bigMoverPreferences.listenForBigMoverNotificationChanged().first()

        return@withContext BigMoverAlarm(
            params,
            isEnabled,
        )
      }

  override suspend fun refresherAlarm(params: RefreshWorkerParameters): Alarm =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext RefresherAlarm(params)
      }
}
