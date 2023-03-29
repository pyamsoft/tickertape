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

package com.pyamsoft.tickertape.alert.notification

import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.tickertape.stocks.api.StockSymbol
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCanceller
@Inject
internal constructor(
    private val notifier: Notifier,
    private val idMap: NotificationIdMap,
) {

  fun cancelBigMoverNotification(symbol: StockSymbol) {
    val id = idMap.getNotificationId(NotificationType.BIG_MOVER) { symbol }
    Timber.d("Cancel big mover notification: $symbol $id")
    notifier.cancel(id)
  }
}
