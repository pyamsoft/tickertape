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

package com.pyamsoft.tickertape.tape.remote

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
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.tape.TapeInternalApi
import com.pyamsoft.tickertape.tape.TapePreferences
import com.pyamsoft.tickertape.tape.notification.TapeNotificationData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class TapeRemoteImpl
@Inject
internal constructor(
    @TapeInternalApi private val stopBus: EventConsumer<TapeRemote.StopCommand>,
    @TapeInternalApi private val notifier: Notifier,
    private val preferences: TapePreferences,
    private val symbolQueryDao: SymbolQueryDao,
    private val symbolQueryDaoCache: SymbolQueryDao.Cache,
    private val interactor: StockInteractor,
    private val interactorCache: StockInteractor.Cache,
) : TapeRemote {

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

  override suspend fun watchPageSize(onChange: (Int) -> Unit) =
      withContext(context = Dispatchers.IO) {
        preferences.listenForTapePageSizeChanged().collectLatest { pageSize ->
          withContext(context = Dispatchers.Main) { onChange(pageSize) }
        }
      }

  override suspend fun onStopReceived(onStop: () -> Unit) =
      withContext(context = Dispatchers.IO) {
        stopBus.onEvent { withContext(context = Dispatchers.Main) { onStop() } }
      }

  override fun createNotification(service: Service) {
    notifier
        .startForeground(
            service = service,
            channelInfo = CHANNEL_INFO,
            id = NOTIFICATION_ID,
            notification = EMPTY_TAPE_NOTIFICATION,
        )
        .also { Timber.d("Started foreground notification: $it") }
    return
  }

  override suspend fun updateNotification(
      service: Service,
      options: TapeRemote.NotificationOptions
  ) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        fetchQuotes(force = options.forceRefresh)
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
                              pageSize = options.pageSize,
                          ),
                  )
                  .also { Timber.d("Update tape notification: $it") }
            }
            .onFailure { Timber.e(it, "Unable to refresh notification") }

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
            pageSize = 0,
        )

    private val CHANNEL_INFO =
        NotifyChannelInfo(
            id = "channel_tickers_foreground_v1",
            title = "My Watchlist",
            description = "My Watchlist",
        )
  }
}