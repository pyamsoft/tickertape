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

package com.pyamsoft.tickertape.alert.workmanager.worker

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.pyamsoft.tickertape.alert.inject.BaseInjector
import com.pyamsoft.tickertape.alert.inject.RefresherInjector
import com.pyamsoft.tickertape.alert.params.RefreshParameters
import com.pyamsoft.tickertape.alert.work.alarm.RefresherAlarm

internal class RefresherWorker internal constructor(context: Context, params: WorkerParameters) :
    BaseWorker<RefreshParameters>(context.applicationContext, params) {

  override fun getInjector(context: Context): BaseInjector<RefreshParameters> {
    return RefresherInjector(context.applicationContext)
  }

  override fun getParams(data: Data): RefreshParameters {
    return RefreshParameters(forceRefresh = data.getBoolean(RefresherAlarm.FORCE_REFRESH, false))
  }
}
