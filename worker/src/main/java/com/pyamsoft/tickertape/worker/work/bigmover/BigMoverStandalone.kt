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

package com.pyamsoft.tickertape.worker.work.bigmover

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.NotifyGuard
import com.pyamsoft.tickertape.db.DbInsert
import com.pyamsoft.tickertape.db.getQuotesForHoldings
import com.pyamsoft.tickertape.db.holding.HoldingQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverInsertDao
import com.pyamsoft.tickertape.db.mover.BigMoverQueryDao
import com.pyamsoft.tickertape.db.mover.BigMoverReport
import com.pyamsoft.tickertape.db.mover.JsonMappableBigMoverReport
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.api.EquityType
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockPercent
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.asPercent
import com.pyamsoft.tickertape.worker.notification.NotificationIdMap
import com.pyamsoft.tickertape.worker.notification.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BigMoverStandalone
@Inject
internal constructor(
    private val notifier: Notifier,
    private val holdingQueryDao: HoldingQueryDao,
    private val stockInteractor: StockInteractor,
    private val enforcer: ThreadEnforcer,
    private val guard: NotifyGuard,
    private val bigMoverQueryDao: BigMoverQueryDao,
    private val bigMoverInsertDao: BigMoverInsertDao,
    private val idMap: NotificationIdMap,
    private val clock: Clock,
) {

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
    enforcer.assertOffMainThread()

    val now = LocalDateTime.now(clock)
    val alreadySeenBigMovers = bigMoverQueryDao.query()

    bigMovers.forEach { quote ->
      val moverRecord = alreadySeenBigMovers.firstOrNull { it.symbol == quote.symbol }
      val insertRecord =
          if (moverRecord == null) {
            // If no mover record exists yet, make a new one
            JsonMappableBigMoverReport.create(quote, clock)
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
      when (val record = bigMoverInsertDao.insert(insertRecord)) {
        is DbInsert.InsertResult.Fail -> Timber.e(record.error, "Failed to record Big move")
        is DbInsert.InsertResult.Insert ->
            Timber.d("Tracking new big move: ${record.data.symbol.raw}")
        is DbInsert.InsertResult.Update -> Timber.d("Updated big move: ${record.data.symbol.raw}")
      }

      postNotification(quote)
    }
  }

  private fun postNotification(quote: StockQuote) {
    if (!guard.canPostNotification()) {
      Timber.w("Missing notification permission, cannot post: $quote")
      return
    }

    val id = idMap.getNotificationId(NotificationType.BIG_MOVER) { quote.symbol }
    notifier
        .show(
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

          // Different threshold percentage based on type
          val up: StockPercent
          val down: StockPercent
          when (quote.type) {
            EquityType.STOCK -> {
              up = STOCK_UP_PERCENT
              down = STOCK_DOWN_PERCENT
            }
            EquityType.OPTION -> {
              up = OPTION_UP_PERCENT
              down = OPTION_DOWN_PERCENT
            }
            EquityType.CRYPTOCURRENCY -> {
              up = CRYPTO_UP_PERCENT
              down = CRYPTO_DOWN_PERCENT
            }
          }

          return@filter value.compareTo(up) > 0 || value.compareTo(down) < 1
        }
        .toList()
  }

  suspend fun notifyBigMovers(quotes: List<StockQuote>) =
      withContext(context = Dispatchers.IO) {
        val bigMovers = quotes.filterBigMovers()
        postNotifications(bigMovers)
      }

  suspend fun notifyBigMovers() =
      withContext(context = Dispatchers.IO) {
        val quotes = stockInteractor.getQuotesForHoldings(holdingQueryDao)
        notifyBigMovers(quotes)
      }

  companion object {

    private val STOCK_DOWN_PERCENT = (-10.0).asPercent()
    private val STOCK_UP_PERCENT = 10.0.asPercent()

    private val OPTION_DOWN_PERCENT = (-30.0).asPercent()
    private val OPTION_UP_PERCENT = 30.0.asPercent()

    private val CRYPTO_DOWN_PERCENT = (-25.0).asPercent()
    private val CRYPTO_UP_PERCENT = 25.0.asPercent()

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
