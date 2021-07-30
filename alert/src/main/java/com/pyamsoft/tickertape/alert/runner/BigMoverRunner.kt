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

package com.pyamsoft.tickertape.alert.runner

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.tickertape.alert.AlertInternalApi
import com.pyamsoft.tickertape.alert.notification.BigMoverNotificationData
import com.pyamsoft.tickertape.alert.notification.NotificationIdMap
import com.pyamsoft.tickertape.alert.notification.NotificationType
import com.pyamsoft.tickertape.alert.params.BigMoverParameters
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotedStock
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.currentSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

@Singleton
internal class BigMoverRunner
@Inject
internal constructor(
    @param:AlertInternalApi private val notifier: Notifier,
    private val idMap: NotificationIdMap,
    private val quoteInteractor: QuoteInteractor,
) : BaseRunner<BigMoverParameters>() {

  override suspend fun performWork(params: BigMoverParameters) = coroutineScope {
    val force = params.forceRefresh
    quoteInteractor
        .getWatchlistQuotes(force)
        .onFailure { Timber.e(it, "Error refreshing quotes") }
        .recover { emptyList() }
        .map { it.filterBigMovers() }
        .onSuccess { postNotifications(it) }
        .onFailure { Timber.e(it, "Error getting big mover quotes") }

    return@coroutineScope
  }

  private fun postNotifications(bigMovers: List<StockQuote>) {
    bigMovers.forEach { mover ->
      val id = idMap.getNotificationId(NotificationType.BIG_MOVER) { mover.symbol() }
      notifier.show(
          id = id,
          channelInfo = CHANNEL_INFO,
          notification = BigMoverNotificationData(quote = mover))
    }
  }

  @CheckResult
  private fun List<QuotedStock>.filterBigMovers(): List<StockQuote> {
    return this.asSequence()
        .mapNotNull { it.quote }
        .filter { quote ->
          val session = quote.currentSession()
          val value = session.percent().value()
          return@filter value.compareTo(10.0) > 0 || value.compareTo(-10.0) < 1
        }
        .toList()
  }

  companion object {

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_big_movers", title = "Big Movers", description = "Big Mover Alerts")
  }
}
