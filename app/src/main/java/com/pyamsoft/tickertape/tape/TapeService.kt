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
import android.content.Intent
import android.os.IBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.tickertape.TickerComponent
import com.pyamsoft.tickertape.receiver.BootReceiver
import com.pyamsoft.tickertape.receiver.ScreenReceiver
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

class TapeService : Service() {

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

    tapeRemote.requireNotNull().also { remote ->
      serviceScope.launch(context = Dispatchers.Default) {
        remote.onStopReceived {
          Timber.w("Stop command received from remote. Stop TapeService")
          stopSelf()
        }
      }

      remote.createNotification(this)
    }

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
      tapeRemote
          .requireNotNull()
          .updateNotification(
              TapeRemote.NotificationOptions(index = index, forceRefresh = forceRefresh))
    }
  }

  override fun onDestroy() {
    super.onDestroy()

    Timber.d("Stop notification in foreground and kill service")
    tapeRemote.requireNotNull().stopNotification(this)

    stopSelf()

    screenReceiverRegistration?.unregister()
    screenReceiverRegistration = null

    if (BootReceiver.isEnabled(this)) {
      BootReceiver.setEnabled(this, false)
    }

    serviceScope.cancel()

    tapeRemote = null
  }

  companion object {

    private const val DEFAULT_INDEX = 0
  }
}
