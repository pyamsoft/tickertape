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

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.CheckResult
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotePair
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class TapeRemote
@Inject
internal constructor(
    private val context: Context,
    private val interactor: QuoteInteractor,
    private val activityClass: Class<out Activity>,
    private val serviceClass: Class<out Service>
) {

  private val remoteViews by lazy {
    RemoteViews(context.applicationContext.packageName, R.layout.remote_view)
  }

  private fun guaranteeNotificationChannelExists(notificationManager: NotificationManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationGroup = NotificationChannelGroup(CHANNEL_ID, CHANNEL_TITLE)
      val notificationChannel =
          NotificationChannel(CHANNEL_ID, CHANNEL_TITLE, NotificationManager.IMPORTANCE_DEFAULT)
              .apply {
                group = notificationGroup.id
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                description = CHANNEL_DESCRIPTION
                enableLights(false)
                enableVibration(true)
              }

      Timber.d("Create notification channel and group: $CHANNEL_ID $CHANNEL_TITLE")
      notificationManager.apply {
        createNotificationChannelGroup(notificationGroup)
        createNotificationChannel(notificationChannel)
      }
    }
  }

  @CheckResult
  private fun getActivityPendingIntent(): PendingIntent {
    val appContext = context.applicationContext
    val activityIntent =
        Intent(appContext, activityClass).apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    return PendingIntent.getActivity(
        appContext, REQUEST_CODE_ACTIVITY, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
  }

  @CheckResult
  private fun getServicePendingIntent(
      requestCode: Int,
      options: PendingIntentOptions
  ): PendingIntent {
    val appContext = context.applicationContext
    val serviceIntent =
        Intent(appContext, serviceClass).apply {
          putExtra(KEY_CURRENT_INDEX, options.index)
          putExtra(KEY_FORCE_REFRESH, options.forceRefresh)
        }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      PendingIntent.getForegroundService(
          appContext, requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    } else {
      PendingIntent.getService(
          appContext, requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
  }

  @CheckResult
  private fun getQuoteSession(quote: StockQuote): StockMarketSession {
    val after = quote.afterHours()
    if (after != null) {
      return after
    }

    return quote.regular()
  }

  private fun updateTickerInfo(
      index: Int,
      quotes: List<QuotePair>,
      remoteViewIdGroup: RemoteViewIds
  ) {
    val quote = quotes[index].quote
    if (quote == null) {
      Timber.w("Missing quote for index $index $quotes")
      return
    }

    val session = getQuoteSession(quote)
    val data = StockMarketSession.getDataFromSession(session)
    val percent = data.percent
    val changeAmount = data.changeAmount
    val directionSign = data.directionSign
    val color = data.color
    val priceText = "\$${session.price().asMoneyValue()}"
    val percentText = "(${directionSign}${percent}%)"
    val changeText = "$directionSign${changeAmount}"

    remoteViews.setTextViewText(remoteViewIdGroup.symbolViewId, quote.symbol().symbol())

    remoteViews.setTextViewText(remoteViewIdGroup.priceViewId, priceText)
    remoteViews.setTextColor(remoteViewIdGroup.priceViewId, color)

    remoteViews.setTextViewText(remoteViewIdGroup.changeViewId, changeText)
    remoteViews.setTextColor(remoteViewIdGroup.changeViewId, color)

    remoteViews.setTextViewText(remoteViewIdGroup.percentViewId, percentText)
    remoteViews.setTextColor(remoteViewIdGroup.percentViewId, color)
  }

  private fun updateTickers(quotes: List<QuotePair>, index: Int, pageSize: Int) {
    for (loop in 0 until pageSize) {
      val adjustedIndex = correctIndex(index + loop, quotes.size)
      val remoteViewIdGroup = remoteViewIds[loop]
      updateTickerInfo(adjustedIndex, quotes, remoteViewIdGroup)
    }
  }

  @CheckResult
  private fun generateNotificationAction(
      name: String,
      intent: PendingIntent
  ): NotificationCompat.Action {
    return NotificationCompat.Action.Builder(0, name, intent)
        .setAllowGeneratedReplies(false)
        .setShowsUserInterface(false)
        .setContextual(false)
        .build()
  }

  @CheckResult
  private fun createNotificationBuilder(): NotificationCompat.Builder {
    return NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_code_24dp)
        .setShowWhen(false)
        .setAutoCancel(false)
        .setOngoing(false)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setSilent(true)
  }

  @CheckResult
  private fun hydrateNotification(
      notificationManager: NotificationManager,
      quotes: List<QuotePair>,
      index: Int
  ): Notification {
    guaranteeNotificationChannelExists(notificationManager)

    val builder = createNotificationBuilder()
    if (quotes.isEmpty()) {
      return builder.build()
    }

    val pageSize = getPageSize()
    val safeIndex = correctIndex(index, quotes.size)
    updateTickers(quotes, safeIndex, pageSize)

    return builder
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(remoteViews)
        .addAction(generateNotificationAction("Open", getActivityPendingIntent()))
        .addAction(
            generateNotificationAction(
                "Next",
                getServicePendingIntent(
                    REQUEST_CODE_NEXT,
                    PendingIntentOptions(index = safeIndex + pageSize, forceRefresh = false))))
        .addAction(
            generateNotificationAction(
                "Refresh",
                getServicePendingIntent(
                    REQUEST_CODE_REFRESH,
                    PendingIntentOptions(index = safeIndex, forceRefresh = true))))
        .build()
  }

  @CheckResult
  fun createNotification(notificationManager: NotificationManager): Notification {
    return hydrateNotification(notificationManager, emptyList(), 0)
  }

  @CheckResult
  suspend fun updateNotification(
      notificationManager: NotificationManager,
      options: NotificationOptions
  ): Notification =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        val quotePairs = interactor.getQuotes(options.forceRefresh)
        return@withContext hydrateNotification(notificationManager, quotePairs, options.index)
      }

  private data class RemoteViewIds(
      @IdRes val symbolViewId: Int,
      @IdRes val priceViewId: Int,
      @IdRes val changeViewId: Int,
      @IdRes val percentViewId: Int
  )

  data class NotificationOptions(val index: Int, val forceRefresh: Boolean)

  private data class PendingIntentOptions(val index: Int, val forceRefresh: Boolean)

  companion object {

    @JvmStatic
    @CheckResult
    private fun getPageSize(): Int {
      return remoteViewIds.size
    }

    private val remoteViewIds =
        listOf(
            RemoteViewIds(
                symbolViewId = R.id.remote_views_symbol1,
                priceViewId = R.id.remote_views_price1,
                changeViewId = R.id.remote_views_change1,
                percentViewId = R.id.remote_views_percent1),
            RemoteViewIds(
                symbolViewId = R.id.remote_views_symbol2,
                priceViewId = R.id.remote_views_price2,
                changeViewId = R.id.remote_views_change2,
                percentViewId = R.id.remote_views_percent2),
            RemoteViewIds(
                symbolViewId = R.id.remote_views_symbol3,
                priceViewId = R.id.remote_views_price3,
                changeViewId = R.id.remote_views_change3,
                percentViewId = R.id.remote_views_percent3),
        )

    const val KEY_CURRENT_INDEX = "key_current_index"
    const val KEY_FORCE_REFRESH = "key_force_refresh"

    private const val REQUEST_CODE_ACTIVITY = 69420
    private const val REQUEST_CODE_NEXT = REQUEST_CODE_ACTIVITY + 1
    private const val REQUEST_CODE_REFRESH = REQUEST_CODE_ACTIVITY + 2

    private const val CHANNEL_ID = "channel_tickers_foreground"
    private const val CHANNEL_TITLE = "My Watchlist"
    private const val CHANNEL_DESCRIPTION = "My Watchlist"

    @JvmStatic
    @CheckResult
    private fun correctIndex(index: Int, maxIndex: Int): Int {
      return when {
        index >= maxIndex -> index % maxIndex
        index < 0 -> correctIndex(maxIndex + index, maxIndex)
        else -> index
      }
    }
  }
}
