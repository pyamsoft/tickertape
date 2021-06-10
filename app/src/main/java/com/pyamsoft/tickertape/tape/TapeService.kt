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

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.tickertape.TickerComponent
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class TapeService : Service() {

  /** CoroutineScope for the Service level */
  private val serviceScope = MainScope()

  private val notificationManager by lazy {
    requireNotNull(applicationContext.getSystemService<NotificationManager>())
  }

  @Inject @JvmField internal var tapeRemote: TapeRemote? = null

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()

    Injector.obtainFromApplication<TickerComponent>(this).plusTapeComponent().create().inject(this)

    Timber.d("Start notification in foreground: $NOTIFICATION_ID")
    val notification = requireNotNull(tapeRemote).createNotification(notificationManager)
    startForeground(NOTIFICATION_ID, notification)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val index = intent?.getIntExtra(TapeRemote.KEY_CURRENT_INDEX, DEFAULT_INDEX) ?: DEFAULT_INDEX

    serviceScope.launch(context = Dispatchers.Default) {
      val notification =
          requireNotNull(tapeRemote).updateNotification(notificationManager, index, false)
      withContext(context = Dispatchers.Main) {
        Timber.d("Update notification in foreground: $NOTIFICATION_ID $index")
        notificationManager.notify(NOTIFICATION_ID, notification)
      }
    }

    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()

    Timber.d("Stop notification in foreground and kill service")
    stopForeground(true)
    stopSelf()

    serviceScope.cancel()
  }

  companion object {

    private const val DEFAULT_INDEX = 0
    private const val NOTIFICATION_ID = 42069

    @JvmStatic
    fun start(context: Context) {
      val appContext = context.applicationContext
      val service = Intent(appContext, TapeService::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        appContext.startForegroundService(service)
      } else {
        appContext.startService(service)
      }
    }
  }
}
