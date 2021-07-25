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
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.NotifyData
import com.pyamsoft.pydroid.notify.NotifyDispatcher
import com.pyamsoft.pydroid.notify.NotifyId
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.stocks.api.StockQuote
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class TapeDispatcher
@Inject
internal constructor(
    private val context: Context,
    @StringRes @Named("app_name") private val appNameRes: Int,
    private val activityClass: Class<out Activity>,
    private val serviceClass: Class<out Service>
) : NotifyDispatcher<TapeNotificationData> {

  private val channelCreator by lazy {
    requireNotNull(context.applicationContext.getSystemService<NotificationManager>())
  }

  private fun guaranteeNotificationChannelExists(channelInfo: NotifyChannelInfo) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationGroup = NotificationChannelGroup(channelInfo.id, channelInfo.title)
      val notificationChannel =
          NotificationChannel(
                  channelInfo.id, channelInfo.title, NotificationManager.IMPORTANCE_DEFAULT)
              .apply {
                group = notificationGroup.id
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                description = channelInfo.description
                enableLights(false)
                enableVibration(true)
              }

      Timber.d("Create notification channel and group: ${channelInfo.id} ${channelInfo.title}")
      channelCreator.apply {
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
          putExtra(TapeRemote.KEY_CURRENT_INDEX, options.index)
          putExtra(TapeRemote.KEY_FORCE_REFRESH, options.forceRefresh)
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
      remoteViews: RemoteViews,
      index: Int,
      quotes: List<StockQuote>,
      remoteViewIdGroup: RemoteViewIds
  ) {
    val quote = quotes[index]
    val session = getQuoteSession(quote)
    val percent = session.percent().asPercentValue()
    val changeAmount = session.amount().asMoneyValue()
    val directionSign = session.direction().sign()
    val color = session.direction().color()
    val priceText = session.price().asMoneyValue()
    val percentText = "(${directionSign}${percent})"
    val changeText = "$directionSign${changeAmount}"

    remoteViews.setTextViewText(remoteViewIdGroup.symbolViewId, quote.symbol().symbol())

    remoteViews.setTextViewText(remoteViewIdGroup.priceViewId, priceText)
    remoteViews.setTextColor(remoteViewIdGroup.priceViewId, color)

    remoteViews.setTextViewText(remoteViewIdGroup.changeViewId, changeText)
    remoteViews.setTextColor(remoteViewIdGroup.changeViewId, color)

    remoteViews.setTextViewText(remoteViewIdGroup.percentViewId, percentText)
    remoteViews.setTextColor(remoteViewIdGroup.percentViewId, color)
  }

  private fun updateTickers(
      remoteViews: RemoteViews,
      quotes: List<StockQuote>,
      index: Int,
      pageSize: Int
  ) {
    for (loop in 0 until pageSize) {
      val adjustedIndex = correctIndex(index + loop, quotes.size)
      val remoteViewIdGroup = remoteViewIds[loop]
      updateTickerInfo(remoteViews, adjustedIndex, quotes, remoteViewIdGroup)
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
  private fun createNotificationBuilder(
      channelInfo: NotifyChannelInfo
  ): NotificationCompat.Builder {
    return NotificationCompat.Builder(context.applicationContext, channelInfo.id)
        .setSmallIcon(R.drawable.ic_watchlist_24dp)
        .setShowWhen(false)
        .setAutoCancel(false)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setSilent(true)
        .setContentIntent(getActivityPendingIntent())
  }

  @CheckResult
  private fun hydrateNotification(
      channelInfo: NotifyChannelInfo,
      quotes: List<StockQuote>,
      index: Int
  ): Notification {
    guaranteeNotificationChannelExists(channelInfo)

    val safeIndex: Int
    val builder =
        createNotificationBuilder(channelInfo).apply {
          safeIndex = if (quotes.isEmpty()) index else correctIndex(index, quotes.size)
          addAction(
              generateNotificationAction(
                  "Refresh",
                  getServicePendingIntent(
                      REQUEST_CODE_REFRESH,
                      PendingIntentOptions(index = safeIndex, forceRefresh = true))))
        }

    if (quotes.isEmpty()) {
      return builder
          .setContentTitle(context.getString(appNameRes))
          .setContentText("No quotes. Refresh the notification to view your watchlist.")
          .build()
    }

    val pageSize = getPageSize()

    val remoteViews = RemoteViews(context.applicationContext.packageName, R.layout.remote_view)
    updateTickers(remoteViews, quotes, safeIndex, pageSize)

    return builder
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(remoteViews)
        .addAction(
            generateNotificationAction(
                "Next",
                getServicePendingIntent(
                    REQUEST_CODE_NEXT,
                    PendingIntentOptions(index = safeIndex + pageSize, forceRefresh = false))))
        .build()
  }

  override fun build(
      id: NotifyId,
      channelInfo: NotifyChannelInfo,
      notification: TapeNotificationData
  ): Notification {
    return hydrateNotification(channelInfo, notification.quotes, notification.index)
  }

  override fun canShow(notification: NotifyData): Boolean {
    return notification is TapeNotificationData
  }

  private data class RemoteViewIds(
      @IdRes val symbolViewId: Int,
      @IdRes val priceViewId: Int,
      @IdRes val changeViewId: Int,
      @IdRes val percentViewId: Int
  )

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

    private const val REQUEST_CODE_ACTIVITY = 69420
    private const val REQUEST_CODE_NEXT = REQUEST_CODE_ACTIVITY + 1
    private const val REQUEST_CODE_REFRESH = REQUEST_CODE_ACTIVITY + 2

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
