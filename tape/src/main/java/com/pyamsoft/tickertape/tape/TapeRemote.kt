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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.tickertape.quote.QuoteInteractor
import com.pyamsoft.tickertape.quote.QuotePair
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class TapeRemote
@Inject
internal constructor(
    private val context: Context,
    private val interactor: QuoteInteractor,
    private val activityClass: Class<out Activity>
) {

  private val remoteViews by lazy {
    RemoteViews(context.applicationContext.packageName, R.layout.remote_view)
  }

  private val notificationBuilder by lazy {
    NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_code_24dp)
        .setAutoCancel(false)
        .setOngoing(false)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(remoteViews)
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
  private fun hydrateNotification(
      notificationManager: NotificationManager,
      quotes: List<QuotePair>
  ): Notification {
    guaranteeNotificationChannelExists(notificationManager)

    val appContext = context.applicationContext
    val activityIntent =
        Intent(appContext, activityClass).apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    val pendingIntent =
        PendingIntent.getActivity(
            appContext, REQUEST_CODE, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val text = quotes.firstOrNull()?.symbol?.symbol().orEmpty()
    remoteViews.setTextViewText(R.id.remote_views_text, text)
    remoteViews.setImageViewResource(R.id.remote_view_previous, R.drawable.ic_bug_report_24dp)
    remoteViews.setImageViewResource(R.id.remote_view_next, R.drawable.ic_bug_report_24dp)
    remoteViews.setOnClickPendingIntent(R.id.remote_view_next, pendingIntent)
    remoteViews.setOnClickPendingIntent(R.id.remote_view_previous, pendingIntent)

    return notificationBuilder.build()
  }

  @CheckResult
  fun createNotification(notificationManager: NotificationManager): Notification {
    return hydrateNotification(notificationManager, emptyList())
  }

  @CheckResult
  suspend fun updateNotification(notificationManager: NotificationManager): Notification =
      withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        val quotePairs = interactor.getQuotes(false)
        return@withContext hydrateNotification(notificationManager, quotePairs)
      }

  companion object {

    private const val REQUEST_CODE = 69420
    private const val CHANNEL_ID = "channel_tickers_foreground"
    private const val CHANNEL_TITLE = "My Watchlist"
    private const val CHANNEL_DESCRIPTION = "My Watchlist"
  }
}
