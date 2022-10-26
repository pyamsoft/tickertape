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

package com.pyamsoft.tickertape.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.notification.NotificationCanceller
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.initOnAppStart
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import com.pyamsoft.tickertape.tape.TapeLauncher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MainAlarmLauncher
@Inject
internal constructor(
    private val notificationCanceller: NotificationCanceller,
    private val tapeLauncher: TapeLauncher,
    private val alerter: Alerter,
    private val alarmFactory: AlarmFactory,
) {

  fun cancelNotifications(symbol: StockSymbol) {
    notificationCanceller.cancelBigMoverNotification(symbol)
  }

  fun bind(
      scope: CoroutineScope,
      lifecycle: Lifecycle,
  ) {
    scope.launch(context = Dispatchers.Main) {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) { alerter.initOnAppStart(alarmFactory) }

      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { tapeLauncher.start() }
    }
  }
}
