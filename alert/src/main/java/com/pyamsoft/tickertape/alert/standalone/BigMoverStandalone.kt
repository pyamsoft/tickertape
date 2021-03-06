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

package com.pyamsoft.tickertape.alert.standalone

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.tickertape.alert.AlertInternalApi
import com.pyamsoft.tickertape.alert.notification.BigMoverNotificationData
import com.pyamsoft.tickertape.alert.notification.NotificationIdMap
import com.pyamsoft.tickertape.alert.notification.NotificationType
import com.pyamsoft.tickertape.db.mover.BigMoverInsertDao
import com.pyamsoft.tickertape.db.mover.BigMoverQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverReport
import com.pyamsoft.tickertape.db.mover.JsonMappableBigMoverReport
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.asPercent
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class BigMoverStandalone
@Inject
internal constructor(
    @AlertInternalApi private val notifier: Notifier,
    private val bigMoverQueryDao: BigMoverQueryDao,
    private val bigMoverInsertDao: BigMoverInsertDao,
    private val idMap: NotificationIdMap,
) {

  suspend fun notifyForBigMovers(quotes: List<StockQuote>) {
    val bigMovers = quotes.filterBigMovers()
    postNotifications(bigMovers)
  }

  @CheckResult
  private fun BigMoverReport.updateToSession(
      now: LocalDateTime,
      session: StockMarketSession,
  ): BigMoverReport {
    return this.lastNotified(now)
        .lastPercent(session.percent)
        .lastPrice(session.price)
        .lastState(session.state)
  }

  private suspend fun postNotifications(bigMovers: List<StockQuote>) {
    Enforcer.assertOffMainThread()

    val now = LocalDateTime.now()
    val allQuotes = withContext(context = Dispatchers.IO) { bigMoverQueryDao.query(false) }

    bigMovers.forEach { quote ->
      val moverRecord = allQuotes.firstOrNull { it.symbol == quote.symbol }
      val insertRecord =
          if (moverRecord == null) {
            // If no mover record exists yet, make a new one
            JsonMappableBigMoverReport.create(quote)
          } else {
            val session = quote.currentSession
            if (moverRecord.lastState != session.state) {
              // State has changed, update the record
              moverRecord.updateToSession(now, session)
            } else {
              if (now.minusHours(NOTIFY_PERIOD).isAfter(moverRecord.lastNotified)) {
                // Was last notified over PERIOD hours ago, notify again
                moverRecord.updateToSession(now, session)
              } else {
                // Was last notified within PERIOD hours, do not notify again.
                null
              }
            }
          }

      if (insertRecord == null) {
        Timber.w("Not showing repeat big mover notification for: ${quote.symbol}")
        return@forEach
      }

      // Insert the record so that we can avoid future noisy big mover updates.
      withContext(context = Dispatchers.IO) { bigMoverInsertDao.insert(insertRecord) }

      postNotification(quote)
    }
  }

  private fun postNotification(quote: StockQuote) {
    val id = idMap.getNotificationId(NotificationType.BIG_MOVER) { quote.symbol }
    notifier.show(
            id = id,
            channelInfo = CHANNEL_INFO,
            notification = BigMoverNotificationData(quote = quote),
        )
        .also { Timber.d("Posted big mover notification: $it") }
  }

  @CheckResult
  private fun List<StockQuote>.filterBigMovers(): List<StockQuote> {
    return this.asSequence()
        .filter { quote ->
          val session = quote.currentSession
          val value = session.percent
          return@filter value.compareTo(BIG_MOVER_UP_PERCENT) > 0 ||
              value.compareTo(BIG_MOVER_DOWN_PERCENT) < 1
        }
        .toList()
  }

  companion object {

    private val BIG_MOVER_DOWN_PERCENT = (-10.0).asPercent()
    private val BIG_MOVER_UP_PERCENT = 10.0.asPercent()

    // Don't notify between periods
    private const val NOTIFY_PERIOD = 6L

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_big_movers",
            title = "Big Movers",
            description = "Big Mover Alerts",
        )
  }
}
