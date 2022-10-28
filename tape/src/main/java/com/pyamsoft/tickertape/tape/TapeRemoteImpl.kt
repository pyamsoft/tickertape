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
import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.toNotifyId
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.tickertape.db.getWatchListQuotes
import com.pyamsoft.tickertape.db.symbol.SymbolQueryDao
import com.pyamsoft.tickertape.stocks.StockInteractor
import com.pyamsoft.tickertape.stocks.StockMarket
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
    @TapeInternalApi private val stopBus: EventConsumer<TapeRemote.StopCommand>,
    @TapeInternalApi private val notifier: Notifier,
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolQueryDaoCache: SymbolQueryDao.Cache,
    private val interactor: StockInteractor,
    private val interactorCache: StockInteractor.Cache,
) : TapeRemote {

  override suspend fun onStopReceived(onStop: () -> Unit) =
      withContext(context = Dispatchers.IO) { stopBus.onEvent { onStop() } }

  override fun createNotification(service: Service) {
    val notification =
        if (StockMarket.isOpen()) EMPTY_TAPE_NOTIFICATION else TapeNotificationData.Closed

    notifier
        .startForeground(
            service = service,
            channelInfo = CHANNEL_INFO,
            id = NOTIFICATION_ID,
            notification = notification,
        )
        .also { id -> Timber.d("Started foreground notification: $id") }
    return
  }

  @CheckResult
  private suspend fun fetchQuotes(force: Boolean): ResultWrapper<List<StockQuote>> {
    val result =
        try {
          if (force) {
            symbolQueryDaoCache.invalidate()
            interactorCache.invalidateAllQuotes()
          }

          val quotes = interactor.getWatchListQuotes(symbolQueryDao)
          ResultWrapper.success(quotes)
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error fetching quotes")
            ResultWrapper.failure(e)
          }
        }

    return result.recover { emptyList() }
  }

  override suspend fun updateNotification(
      service: Service,
      options: TapeRemote.NotificationOptions
  ) =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()

        if (StockMarket.isOpen()) {
          val force = options.forceRefresh
          fetchQuotes(force)
              .onSuccess { quotes ->
                notifier
                    .startForeground(
                        service = service,
                        id = NOTIFICATION_ID,
                        channelInfo = CHANNEL_INFO,
                        notification =
                            TapeNotificationData.Quotes(
                                quotes = quotes,
                                index = options.index,
                            ),
                    )
                    .also { Timber.d("Update tape notification: $it") }
              }
              .onSuccess { Timber.d("Updated foreground notification $NOTIFICATION_ID") }
              .onFailure { Timber.e(it, "Unable to refresh notification") }
        } else {
          notifier
              .startForeground(
                  service = service,
                  id = NOTIFICATION_ID,
                  channelInfo = CHANNEL_INFO,
                  notification = TapeNotificationData.Closed,
              )
              .also { Timber.d("Update tape notification: $it") }
        }

        return@withContext
      }

  override fun stopNotification(service: Service) {
    notifier.stopForeground(service, NOTIFICATION_ID)
  }

  companion object {

    private val NOTIFICATION_ID = 42069.toNotifyId()
    private val EMPTY_TAPE_NOTIFICATION =
        TapeNotificationData.Quotes(
            quotes = emptyList(),
            index = 0,
        )

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_tickers_foreground_v1",
            title = "My Watchlist",
            description = "My Watchlist",
        )
  }
}
