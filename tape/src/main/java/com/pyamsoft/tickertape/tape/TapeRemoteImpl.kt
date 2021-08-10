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

package com.pyamsoft.tickertape.tape

import android.app.Service
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventConsumer
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.toNotifyId
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.stocks.api.StockQuote
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class TapeRemoteImpl
@Inject
internal constructor(
    @param:TapeInternalApi private val stopBus: EventConsumer<TapeRemote.StopCommand>,
    @param:TapeInternalApi private val notifier: Notifier,
    private val interactor: QuoteInteractor,
) : TapeRemote {

  override suspend fun onStopReceived(onStop: () -> Unit) =
      withContext(context = Dispatchers.IO) { stopBus.onEvent { onStop() } }

  override fun createNotification(service: Service) {
    notifier.startForeground(
            service = service,
            channelInfo = CHANNEL_INFO,
            id = NOTIFICATION_ID,
            notification = EMPTY_TAPE_NOTIFICATION)
        .also { id -> Timber.d("Started foreground notification: $id") }
    return
  }

  @CheckResult
  private suspend fun fetchQuotes(force: Boolean): ResultWrapper<List<StockQuote>> {
    return try {
      interactor
          .getWatchlistQuotes(force)
          .map { quotes ->
            quotes
                .asSequence()
                .filterNot { it.quote == null }
                .map { it.quote.requireNotNull() }
                .toList()
          }
          .onFailure { Timber.e(it, "Failed to fetch watchlist quotes") }
          .recover { emptyList() }
    } catch (e: Throwable) {
      Timber.e(e, "Error fetching quotes")
      ResultWrapper.failure(e)
    }
  }

  override suspend fun updateNotification(options: TapeRemote.NotificationOptions) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()

        val force = options.forceRefresh
        fetchQuotes(force)
            .onSuccess { quotes ->
              notifier.show(
                  id = NOTIFICATION_ID,
                  channelInfo = CHANNEL_INFO,
                  notification = TapeNotificationData(quotes = quotes, index = options.index))
            }
            .onSuccess { Timber.d("Updated foreground notification $NOTIFICATION_ID") }
            .onFailure { Timber.e(it, "Unable to refresh notification") }

        // Unit
        return@withContext
      }

  override fun stopNotification(service: Service) {
    notifier.stopForeground(service, NOTIFICATION_ID)
  }

  companion object {

    private val NOTIFICATION_ID = 42069.toNotifyId()
    private val EMPTY_TAPE_NOTIFICATION = TapeNotificationData(quotes = emptyList(), index = 0)

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_tickers_foreground", title = "My Watchlist", description = "My Watchlist")
  }
}
