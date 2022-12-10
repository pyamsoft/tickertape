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
import android.content.res.Configuration
import android.os.IBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.ObjectGraph
import com.pyamsoft.tickertape.receiver.BootReceiver
import com.pyamsoft.tickertape.receiver.ScreenReceiver
import com.pyamsoft.tickertape.tape.remote.TapeRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TapeService : Service() {

  /** The custom notification */
  @Inject @JvmField internal var tapeRemote: TapeRemote? = null

  /** CoroutineScope for the Service level */
  private val serviceScope = MainScope()

  /**
   * Watch the screen ON state
   *
   * Will refresh the tape on screen ON via TapeLauncher
   */
  private var screenReceiverRegistration: ScreenReceiver.Registration? = null

  /** Tape Notification bits */
  private var currentIndex = DEFAULT_INDEX
  private var pageSize = TapePreferences.VALUE_DEFAULT_PAGE_SIZE

  private fun updateTape(intent: Intent?) {
    val self = this

    // Resolve current options from intent if possible, otherwise keep current
    val forceRefresh = intent?.getBooleanExtra(TapeRemote.KEY_FORCE_REFRESH, false) ?: false
    val index = intent?.getIntExtra(TapeRemote.KEY_CURRENT_INDEX, currentIndex) ?: currentIndex
    currentIndex = index

    serviceScope.launch(context = Dispatchers.Main) {
      tapeRemote
          .requireNotNull()
          .updateNotification(
              service = self,
              options =
                  TapeRemote.NotificationOptions(
                      index = index,
                      forceRefresh = forceRefresh,
                      pageSize = pageSize,
                  ),
          )
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()

    ObjectGraph.ApplicationScope.retrieve(this).plusTapeComponent().create().inject(this)

    tapeRemote.requireNotNull().also { remote ->
      // Launch notification immediately
      remote.createNotification(this)

      // Also open for listening for Stop commands
      serviceScope.launch(context = Dispatchers.Main) {
        remote.onStopReceived {
          Timber.w("Stop command received from remote. Stop TapeService")
          stopSelf()
        }
      }

      // Also listen to page size
      serviceScope.launch(context = Dispatchers.Main) {
        remote.watchPageSize { newPageSize ->
          pageSize = newPageSize

          // Update the notification
          updateTape(null)
        }
      }
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

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)

    // Update tape notification on config change, but no intent to go off of
    updateTape(null)
  }

  companion object {

    private const val DEFAULT_INDEX = 0
  }
}
