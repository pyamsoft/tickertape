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

import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverWorkerParameters
import com.pyamsoft.tickertape.alert.types.pricealert.PriceAlertWorkerParameters
import com.pyamsoft.tickertape.alert.types.refresh.RefreshWorkerParameters

suspend fun Alerter.initOnAppStart(factory: AlarmFactory) {
  cancel()

  // Periodically refresh the Tape
  scheduleAlarm(
      factory.refresherAlarm(
          RefreshWorkerParameters(
              forceRefresh = false,
          ),
      ),
  )

  // Periodically check for big movers
  scheduleAlarm(
      factory.bigMoverAlarm(
          BigMoverWorkerParameters(
              forceRefresh = false,
          ),
      ),
  )

  // Periodically check for price alerts
  scheduleAlarm(
      factory.priceAlertAlarm(
          PriceAlertWorkerParameters(
              forceRefresh = false,
          ),
      ),
  )
}
