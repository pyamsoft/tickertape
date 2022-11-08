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

package com.pyamsoft.tickertape.alert.types.pricealert

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.NotifyGuard
import com.pyamsoft.tickertape.alert.AlertInternalApi
import com.pyamsoft.tickertape.alert.notification.NotificationIdMap
import com.pyamsoft.tickertape.alert.notification.NotificationType
import com.pyamsoft.tickertape.db.pricealert.PriceAlert
import com.pyamsoft.tickertape.db.pricealert.PriceAlertInsertDao
import com.pyamsoft.tickertape.db.pricealert.PriceAlertQueryDao
import com.pyamsoft.tickertape.stocks.api.StockQuote
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class PriceAlertStandalone
@Inject
internal constructor(
    @AlertInternalApi private val notifier: Notifier,
    private val guard: NotifyGuard,
    private val priceAlertQueryDao: PriceAlertQueryDao,
    private val priceAlertInsertDao: PriceAlertInsertDao,
    private val idMap: NotificationIdMap,
) {

  @CheckResult
  private fun PriceAlert.markAsAlerted(
      now: LocalDateTime,
  ): PriceAlert {
    return this.lastNotified(now).disable()
  }

  private fun postNotification(
      quote: StockQuote,
      alert: PriceAlert,
  ) {
    if (!guard.canPostNotification()) {
      Timber.w("Missing notification permission, cannot post: $quote")
      return
    }

    val id = idMap.getNotificationId(NotificationType.PRICE_ALERT) { quote.symbol }
    notifier
        .show(
            id = id,
            channelInfo = CHANNEL_INFO,
            notification =
                PriceAlertNotificationData(
                    quote = quote,
                    alert = alert,
                ),
        )
        .also { Timber.d("Posted price alert notification: $it") }
  }

  suspend fun notifyPriceAlerts(quotes: List<StockQuote>) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val now = LocalDateTime.now()
        // For each alert in price alerts
        // If the price of the stock quote has passed an alert direction
        // mark as alerted and trigger alert notification
        val alerts =
            priceAlertQueryDao.query().filter {
              // Alert must be enabled
              // Alert must have a trigger price set in some direction
              it.enabled && (it.triggerPriceAbove != null || it.triggerPriceBelow != null)
            }
        Timber.d("FUTURE: PROCESS PRICE ALERTS: $quotes $alerts")
      }

  companion object {

    // Don't notify between periods
    private const val NOTIFY_PERIOD = 6L

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_price_alerts",
            title = "Price Alerts",
            description = "Stock Price Alerts",
        )
  }
}
