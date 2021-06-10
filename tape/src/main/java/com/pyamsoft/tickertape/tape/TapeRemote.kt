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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.CheckResult
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
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
  private fun getServicePendingIntent(index: Int, requestCode: Int): PendingIntent {
    val appContext = context.applicationContext
    val serviceIntent =
        Intent(appContext, serviceClass).apply { putExtra(KEY_CURRENT_INDEX, index) }
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
    val color = getTradeColor(session)
    val priceText = "\$${session.price().value()}"
    remoteViews.setTextViewText(remoteViewIdGroup.symbolViewId, quote.symbol().symbol())
    remoteViews.setTextViewText(remoteViewIdGroup.priceViewId, priceText)
    remoteViews.setTextColor(remoteViewIdGroup.priceViewId, color)
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
  private fun createNotificationBuilder(icon: SmallIcon?): NotificationCompat.Builder {
    return NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
        .apply {
          if (icon == null) {
            setSmallIcon(R.drawable.ic_code_24dp)
          } else {
            setSmallIcon(textAsIcon(icon))
          }
        }
        .setShowWhen(false)
        .setAutoCancel(false)
        .setOngoing(false)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setSilent(true)
  }

  @CheckResult
  private fun resolveSmallIcon(index: Int, quotes: List<QuotePair>): SmallIcon? {
    if (quotes.isEmpty()) {
      return null
    }

    val quote = quotes[index].quote
    if (quote == null) {
      Timber.w("Missing quote for index $index $quotes")
      return null
    }

    return SmallIcon(
        symbol = quote.symbol().symbol(), price = getQuoteSession(quote).price().value())
  }

  @CheckResult
  private fun hydrateNotification(
      notificationManager: NotificationManager,
      quotes: List<QuotePair>,
      index: Int
  ): Notification {
    guaranteeNotificationChannelExists(notificationManager)

    if (quotes.isEmpty()) {
      return createNotificationBuilder(null).build()
    }

    val pageSize = getPageSize()
    val safeIndex = correctIndex(index * pageSize, quotes.size)
    updateTickers(quotes, safeIndex, pageSize)

    val smallIcon = resolveSmallIcon(correctIndex(index, quotes.size), quotes)

    return createNotificationBuilder(smallIcon)
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(remoteViews)
        .addAction(generateNotificationAction("Open", getActivityPendingIntent()))
        .addAction(
            generateNotificationAction(
                "Next",
                getServicePendingIntent(correctIndex(index, quotes.size) + 1, REQUEST_CODE_NEXT)))
        .build()
  }

  @CheckResult
  fun createNotification(notificationManager: NotificationManager): Notification {
    return hydrateNotification(notificationManager, emptyList(), 0)
  }

  @CheckResult
  suspend fun updateNotification(
      notificationManager: NotificationManager,
      index: Int,
      force: Boolean
  ): Notification =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        val quotePairs = interactor.getQuotes(force)
        return@withContext hydrateNotification(notificationManager, quotePairs, index)
      }

  private data class RemoteViewIds(@IdRes val symbolViewId: Int, @IdRes val priceViewId: Int)

  private data class SmallIcon(val symbol: String, val price: String)

  companion object {

    private const val NOTIFICATION_SIZE = 64

    private val symbolPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          textSize = NOTIFICATION_SIZE / 2.5F
          color = Color.WHITE
          textAlign = Paint.Align.LEFT
        }

    private val pricePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          textSize = NOTIFICATION_SIZE / 3.5F
          color = Color.WHITE
          textAlign = Paint.Align.LEFT
        }

    @JvmStatic
    @CheckResult
    private fun textAsIcon(icon: SmallIcon): IconCompat {

      val image = Bitmap.createBitmap(NOTIFICATION_SIZE, NOTIFICATION_SIZE, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(image)

      canvas.drawText(icon.symbol, 0F, NOTIFICATION_SIZE / 2F, symbolPaint)
      canvas.drawText(icon.price, 0F, NOTIFICATION_SIZE * 0.8F, pricePaint)

      return IconCompat.createWithBitmap(image)
    }

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
            ),
            RemoteViewIds(
                symbolViewId = R.id.remote_views_symbol2,
                priceViewId = R.id.remote_views_price2,
            ),
            RemoteViewIds(
                symbolViewId = R.id.remote_views_symbol3,
                priceViewId = R.id.remote_views_price3,
            ),
            RemoteViewIds(
                symbolViewId = R.id.remote_views_symbol4,
                priceViewId = R.id.remote_views_price4,
            ),
        )

    const val KEY_CURRENT_INDEX = "key_current_index"

    private const val REQUEST_CODE_ACTIVITY = 69420
    private const val REQUEST_CODE_NEXT = REQUEST_CODE_ACTIVITY + 1

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

    @JvmStatic
    @CheckResult
    private fun getTradeColor(session: StockMarketSession): Int {
      return when {
        session.direction().isZero() -> Color.WHITE
        session.direction().isUp() -> Color.GREEN
        else -> Color.RED
      }
    }
  }
}
