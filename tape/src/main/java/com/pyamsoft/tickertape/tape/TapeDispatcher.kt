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
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.NotifyData
import com.pyamsoft.pydroid.notify.NotifyDispatcher
import com.pyamsoft.pydroid.notify.NotifyId
import com.pyamsoft.tickertape.stocks.api.StockMoneyValue
import com.pyamsoft.tickertape.stocks.api.StockQuote
import com.pyamsoft.tickertape.stocks.api.asMoney
import com.pyamsoft.tickertape.ui.R as R2
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
    context.applicationContext.getSystemService<NotificationManager>().requireNotNull()
  }

  private val displayDensity by lazy {
    val res = context.resources
    res.displayMetrics.density
  }

  // The width of a data view in DP
  private val dataWidthAmount by lazy {
    val res = context.resources
    res.getDimension(R.dimen.remote_view_data_width) / displayDensity
  }

  // The width of a medium data view in DP
  private val mediumDataWidthAmount by lazy {
    val res = context.resources
    res.getDimension(R.dimen.remote_view_data_width_medium) / displayDensity
  }

  private val largeDataWidthAmount by lazy {
    val res = context.resources
    res.getDimension(R.dimen.remote_view_data_width_large) / displayDensity
  }

  // The width of a data margin in DP
  private val marginWidthAmount by lazy {
    val res = context.resources
    res.getDimension(R.dimen.remote_view_data_margin) / displayDensity
  }

  private fun guaranteeNotificationChannelExists(channelInfo: NotifyChannelInfo) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationGroup =
          NotificationChannelGroup("${channelInfo.id} Group", "${channelInfo.title} Group")
      val notificationChannel =
          NotificationChannel(channelInfo.id, channelInfo.title, NotificationManager.IMPORTANCE_MIN)
              .apply {
                group = notificationGroup.id
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                description = channelInfo.description
                enableLights(false)
                enableVibration(true)
              }

      Timber.d("Create notification channel and group: ${channelInfo.id} ${channelInfo.title}")
      channelCreator.apply {
        // Delete the group if it already exists with a bad group ID
        // Group ID and channel ID cannot match
        if (notificationChannelGroups.firstOrNull { it.id == channelInfo.id } != null) {
          deleteNotificationChannelGroup(channelInfo.id)
        }
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
        appContext,
        REQUEST_CODE_ACTIVITY,
        activityIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
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
          appContext,
          requestCode,
          serviceIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
    } else {
      PendingIntent.getService(
          appContext,
          requestCode,
          serviceIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
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
        .setSmallIcon(R2.drawable.ic_watchlist_24dp)
        .setShowWhen(false)
        .setAutoCancel(false)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setSilent(true)
        .setContentIntent(getActivityPendingIntent())
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
  }

  @CheckResult
  private fun createBuilderWithRefreshAction(
      channelInfo: NotifyChannelInfo,
      index: Int
  ): NotificationCompat.Builder {
    guaranteeNotificationChannelExists(channelInfo)
    return createNotificationBuilder(channelInfo).apply {
      val options = PendingIntentOptions(index = index, forceRefresh = true)
      val pe = getServicePendingIntent(REQUEST_CODE_REFRESH, options)
      val refreshAction = generateNotificationAction("Refresh", pe)
      addAction(refreshAction)
    }
  }

  private fun addRemoteViewQuoteData(
      remoteViews: RemoteViews,
      quote: StockQuote,
  ) {
    val session = quote.currentSession
    val color = session.direction.color
    val priceText = session.price.display

    val layoutId =
        when {
          session.price.isLarge() -> R.layout.remote_view_data_large
          session.price.isMedium() -> R.layout.remote_view_data_medium
          else -> R.layout.remote_view_data
        }

    val view = RemoteViews(context.packageName, layoutId)
    view.setTextViewText(R.id.remote_views_data_symbol, quote.symbol.raw)
    view.setTextViewText(R.id.remote_views_data_price, priceText)
    view.setTextColor(R.id.remote_views_data_price, color)
    remoteViews.addView(R.id.remote_views, view)
  }

  private fun addRemoteViewSpacer(
      remoteViews: RemoteViews,
  ) {
    val view = RemoteViews(context.packageName, R.layout.remote_view_spacer)
    remoteViews.addView(R.id.remote_views, view)
  }

  private fun addRemoteViewMargin(
      remoteViews: RemoteViews,
  ) {
    val view = RemoteViews(context.packageName, R.layout.remote_view_margin)
    remoteViews.addView(R.id.remote_views, view)
  }

  @CheckResult
  private fun getTotalDrawableWidth(): Int {
    // Total screen width
    val screenWidth = context.resources.configuration.screenWidthDp

    // Minus margin available (guess HACK)
    val adjustForMargin = 16 * 2

    // Minus margin available (guess HACK)
    val adjustForNotificationShade = 8 * 2

    return screenWidth - adjustForMargin - adjustForNotificationShade
  }

  @CheckResult
  private fun hydrateRemoteViewTickerData(
      remoteViews: RemoteViews,
      quotes: List<StockQuote>,
      start: Int,
  ): Int {
    // Keep track
    var index = start

    // Starting spacer
    addRemoteViewSpacer(remoteViews)

    val totalAvailableAmount = getTotalDrawableWidth()
    var totalConsumedWidthAmount = 0F

    while (true) {
      val quote = quotes[boundIndex(index, quotes.size)]

      val expectedRequiredAmount =
          when {
            quote.isLarge() -> largeDataWidthAmount
            quote.isMedium() -> mediumDataWidthAmount
            else -> dataWidthAmount
          }

      // Can we still fit another data entry
      if (totalConsumedWidthAmount + expectedRequiredAmount >= totalAvailableAmount) {
        break
      }
      addRemoteViewQuoteData(remoteViews, quote)
      totalConsumedWidthAmount += expectedRequiredAmount
      index += 1

      // Can we still fit another margin
      if (totalConsumedWidthAmount + marginWidthAmount >= totalAvailableAmount) {
        break
      }

      addRemoteViewMargin(remoteViews)
      totalConsumedWidthAmount += marginWidthAmount
    }

    // Ending spacer
    addRemoteViewSpacer(remoteViews)

    return index
  }

  @CheckResult
  private fun buildQuoteNotification(
      builder: NotificationCompat.Builder,
      start: Int,
      quotes: List<StockQuote>
  ): Notification {

    if (quotes.isEmpty()) {
      return builder
          .setContentTitle(context.getString(appNameRes))
          .setContentText("No quotes. Refresh the notification to view your watchlist.")
          .build()
    }

    // Get how many we can show per notifcation "page"
    val remoteViews = RemoteViews(context.packageName, R.layout.remote_view)
    val nextPageStarts = hydrateRemoteViewTickerData(remoteViews, quotes, start)

    val nextOptions = PendingIntentOptions(index = start + nextPageStarts, forceRefresh = false)
    val nextPe = getServicePendingIntent(REQUEST_CODE_NEXT, nextOptions)

    return builder
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(remoteViews)
        .addAction(generateNotificationAction("Next", nextPe))
        .build()
  }

  @CheckResult
  private fun buildClosedNotification(builder: NotificationCompat.Builder): Notification {
    return builder
        .setContentTitle(context.getString(appNameRes))
        .setContentText("Market is closed. Notification will refresh when market is open again.")
        .build()
  }

  @CheckResult
  private fun getBoundQuotePageIndex(notification: TapeNotificationData): Int {
    return when (notification) {
      is TapeNotificationData.Quotes -> {
        val quotes = notification.quotes
        // Safely bind between the size of the list
        if (quotes.isEmpty()) notification.index else boundIndex(notification.index, quotes.size)
      }
      is TapeNotificationData.Closed -> 0
    }
  }

  override fun build(
      id: NotifyId,
      channelInfo: NotifyChannelInfo,
      notification: TapeNotificationData
  ): Notification {
    // Index of the remote view "page"
    val start = getBoundQuotePageIndex(notification)

    // Create builder with Refresh action and pass to customizers
    val builder = createBuilderWithRefreshAction(channelInfo = channelInfo, index = start)

    return when (notification) {
      is TapeNotificationData.Quotes -> buildQuoteNotification(builder, start, notification.quotes)
      is TapeNotificationData.Closed -> buildClosedNotification(builder)
    }
  }

  override fun canShow(notification: NotifyData): Boolean {
    return notification is TapeNotificationData
  }

  private data class PendingIntentOptions(
      // Page index
      val index: Int,

      // Force refresh upon action triggered
      val forceRefresh: Boolean,
  )

  companion object {

    private const val REQUEST_CODE_ACTIVITY = 1337420
    private const val REQUEST_CODE_NEXT = REQUEST_CODE_ACTIVITY + 1
    private const val REQUEST_CODE_REFRESH = REQUEST_CODE_ACTIVITY + 2

    // Once our dollar value is more than 10K, we must use the large layout
    private val LARGE_MONEY = 10_000.00.asMoney()

    // Once our dollar value is more than 1K, we must use the large layout
    private val MEDIUM_MONEY = 1_000.00.asMoney()

    @CheckResult
    private fun StockQuote.isLarge(): Boolean {
      return this.currentSession.price.isLarge()
    }

    @CheckResult
    private fun StockMoneyValue.isLarge(): Boolean {
      return this.compareTo(LARGE_MONEY) >= 0
    }

    @CheckResult
    private fun StockQuote.isMedium(): Boolean {
      return this.currentSession.price.isMedium()
    }

    @CheckResult
    private fun StockMoneyValue.isMedium(): Boolean {
      return this.compareTo(MEDIUM_MONEY) >= 0
    }

    @JvmStatic
    @CheckResult
    private fun boundIndex(index: Int, maxIndex: Int): Int {
      return when {
        index >= maxIndex -> index % maxIndex
        index < 0 -> boundIndex(maxIndex + index, maxIndex)
        else -> index
      }
    }
  }
}
