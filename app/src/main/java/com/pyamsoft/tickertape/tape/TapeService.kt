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
import android.content.Intent
import android.os.IBinder
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.receiver.BootReceiver
import com.pyamsoft.tickertape.receiver.ScreenReceiver
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class TapeService : Service() {

  /** Create and update notificatons */
  private val notificationManager by lazy {
    requireNotNull(applicationContext.getSystemService<NotificationManager>())
  }

  /** CoroutineScope for the Service level */
  private val serviceScope = MainScope()

  /** The custom notification */
  @Inject @JvmField internal var tapeRemote: TapeRemote? = null

  /** The current page of symbol info */
  private var currentIndex = DEFAULT_INDEX

  /** Watch the screen ON state */
  private var screenReceiverRegistration: ScreenReceiver.Registration? = null

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()

    Injector.obtainFromApplication<TickerComponent>(this).plusTapeComponent().create().inject(this)

    Timber.d("Start notification in foreground: $NOTIFICATION_ID")
    val notification = requireNotNull(tapeRemote).createNotification(notificationManager)
    startForeground(NOTIFICATION_ID, notification)

    screenReceiverRegistration = ScreenReceiver.register(this)

    if (!BootReceiver.isEnabled(this)) {
      BootReceiver.setEnabled(this, true)
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    updateTape(intent)
    return START_STICKY
  }

  private fun updateTape(intent: Intent?) {
    val index = intent?.getIntExtra(TapeRemote.KEY_CURRENT_INDEX, currentIndex) ?: currentIndex
    currentIndex = index

    val forceRefresh = intent?.getBooleanExtra(TapeRemote.KEY_FORCE_REFRESH, false) ?: false

    serviceScope.launch(context = Dispatchers.Default) {
      val options = TapeRemote.NotificationOptions(index = index, forceRefresh = forceRefresh)
      val notification = requireNotNull(tapeRemote).updateNotification(notificationManager, options)
      withContext(context = Dispatchers.Main) {
        Timber.d("Update notification in foreground: $NOTIFICATION_ID $options")
        notificationManager.notify(NOTIFICATION_ID, notification)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    Timber.d("Stop notification in foreground and kill service")
    stopForeground(true)
    stopSelf()

    screenReceiverRegistration?.unregister()
    screenReceiverRegistration = null

    if (BootReceiver.isEnabled(this)) {
      BootReceiver.setEnabled(this, false)
    }

    serviceScope.cancel()
  }

  companion object {

    private const val DEFAULT_INDEX = 0
    private const val NOTIFICATION_ID = 42069
  }
}