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

package com.pyamsoft.tickertape.alert.notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.CheckResult
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.NotifyData
import com.pyamsoft.pydroid.notify.NotifyDispatcher
import com.pyamsoft.pydroid.notify.NotifyId
import com.pyamsoft.tickertape.stocks.api.StockMarketSession
import com.pyamsoft.tickertape.tape.R
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class BigMoverNotificationDispatcher
@Inject
internal constructor(private val context: Context, private val activityClass: Class<out Activity>) :
    NotifyDispatcher<BigMoverNotificationData> {

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

  override fun build(
      id: NotifyId,
      channelInfo: NotifyChannelInfo,
      notification: BigMoverNotificationData
  ): Notification {
    guaranteeNotificationChannelExists(channelInfo)

    val quote = notification.quote
    val session: StockMarketSession
    val isAfterHours: Boolean
    val afterHoursSession = quote.afterHours()
    if (afterHoursSession == null) {
      session = quote.regular()
      isAfterHours = false
    } else {
      session = afterHoursSession
      isAfterHours = true
    }

    val percent = session.percent()
    val direction = session.direction()
    val afterHoursString = if (isAfterHours) "after hours" else "so far today"

    @DrawableRes val icon: Int
    val movingString: String
    val directionString: String

    when {
      direction.isUp() -> {
        icon = R.drawable.ic_chart_up_24dp
        movingString = "rising"
        directionString = "up"
      }
      direction.isDown() -> {
        icon = R.drawable.ic_chart_down_24dp
        movingString = "dropping"
        directionString = "down"
      }
      else ->
          throw AssertionError("BigMover notification dispatched without a big move: $notification")
    }

    val title = buildSpannedString {
      bold { append(quote.symbol().symbol()) }
      append(" is $movingString today!")
    }

    val description = buildSpannedString {
      bold { append(quote.symbol().symbol()) }
      append(" is $directionString ")
      bold { append(percent.asPercentValue()) }
      append(" $afterHoursString")
    }

    return NotificationCompat.Builder(context.applicationContext, channelInfo.id)
        .setSmallIcon(icon)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setShowWhen(false)
        .setAutoCancel(false)
        .setContentIntent(getActivityPendingIntent())
        .setContentTitle(title)
        .setContentText(description)
        .build()
  }

  override fun canShow(notification: NotifyData): Boolean {
    return notification is BigMoverNotificationData
  }

  companion object {
    private const val REQUEST_CODE_ACTIVITY = 69420
  }
}
