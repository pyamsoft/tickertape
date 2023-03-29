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

package com.pyamsoft.tickertape.alert.workmanager

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.await
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.tickertape.alert.AlarmParameters
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.base.Alarm
import com.pyamsoft.tickertape.alert.base.PeriodicAlarm
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverAlarm
import com.pyamsoft.tickertape.alert.types.pricealert.PriceAlertAlarm
import com.pyamsoft.tickertape.alert.workmanager.worker.BigMoverWorker
import com.pyamsoft.tickertape.alert.workmanager.worker.PriceAlertWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WorkManagerAlerter
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    private val context: Context,
) : Alerter {

  @CheckResult
  private fun workManager(): WorkManager {
    enforcer.assertOffMainThread()
    return WorkManager.getInstance(context)
  }

  @CheckResult
  private fun Alarm.asWork(): Class<out Worker> {
    val workClass =
        when (this) {
          is BigMoverAlarm -> BigMoverWorker::class.java
          is PriceAlertAlarm -> PriceAlertWorker::class.java
          else -> throw AssertionError("Alarm must be work class $this")
        }

    // Basically, this is shit, but hey its Android!
    // Please make sure your alarms use a class that implements a worker, thanks.
    @Suppress("UNCHECKED_CAST") return workClass as Class<out Worker>
  }

  private suspend fun queueAlarm(alarm: Alarm) {
    enforcer.assertOffMainThread()

    cancelAlarm(alarm)

    if (!alarm.isEnabled()) {
      Timber.w("Not queuing alarm: Alarm is a not enabled: ${alarm.tag()}")
      return
    }

    val tag = alarm.tag()
    val request =
        createWork(
            work = alarm.asWork(),
            tag = tag,
            period = alarm.period(),
            periodUnit = alarm.periodUnit(),
            isPeriodicWork = alarm is PeriodicAlarm,
            inputData = alarm.parameters().toInputData(),
        )

    workManager().enqueue(request)
    Timber.d("Queue work [$tag]: ${request.id}")
  }

  @CheckResult
  private fun generateConstraints(): Constraints {
    enforcer.assertOffMainThread()
    return Constraints.Builder().setRequiresBatteryNotLow(true).build()
  }

  @CheckResult
  private fun createWork(
      work: Class<out Worker>,
      tag: String,
      period: Long,
      periodUnit: TimeUnit,
      isPeriodicWork: Boolean,
      inputData: Data
  ): WorkRequest {
    enforcer.assertOffMainThread()

    return if (isPeriodicWork) {
      PeriodicWorkRequest.Builder(work, period, periodUnit)
          .addTag(tag)
          .setConstraints(generateConstraints())
          .setInputData(inputData)
          .build()
    } else {
      OneTimeWorkRequest.Builder(work)
          .setInitialDelay(period, periodUnit)
          .addTag(tag)
          .setConstraints(generateConstraints())
          .setInputData(inputData)
          .build()
    }
  }

  override suspend fun scheduleAlarm(alarm: Alarm) =
      withContext(context = Dispatchers.Default) { queueAlarm(alarm) }

  override suspend fun cancelAlarm(alarm: Alarm) =
      withContext(context = Dispatchers.Default) {
        workManager().cancelAllWorkByTag(alarm.tag()).await()
        return@withContext
      }

  override suspend fun cancel() =
      withContext(context = Dispatchers.Default) {
        workManager().cancelAllWork().await()
        return@withContext
      }

  companion object {

    @JvmStatic
    @CheckResult
    private fun AlarmParameters.toInputData(): Data {
      var builder = Data.Builder()
      val booleans = this.getBooleanParameters()
      for (entry in booleans) {
        builder = builder.putBoolean(entry.key, entry.value)
      }
      return builder.build()
    }
  }
}
