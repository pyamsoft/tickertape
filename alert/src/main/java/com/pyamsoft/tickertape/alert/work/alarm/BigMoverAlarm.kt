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

import com.pyamsoft.tickertape.alert.work.Alarm
import com.pyamsoft.tickertape.alert.work.AlarmParameters
import java.util.concurrent.TimeUnit

class BigMoverAlarm internal constructor() : Alarm {

  override suspend fun tag(): String {
    return "Big Mover Alarm 1"
  }

  override suspend fun parameters(): AlarmParameters {
    return AlarmParameters.empty()
  }

  override suspend fun period(): Long {
    return TimeUnit.MINUTES.toMillis(15)
  }
}