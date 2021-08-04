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

package com.pyamsoft.tickertape.preference

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.util.PreferenceListener
import com.pyamsoft.pydroid.util.onChange
import com.pyamsoft.tickertape.alert.preference.BigMoverPreferences
import com.pyamsoft.tickertape.tape.TapePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class PreferencesImpl @Inject internal constructor(context: Context) :
    TapePreferences, BigMoverPreferences {

  private val preferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }

  override suspend fun isTapeNotificationEnabled(): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getBoolean(KEY_TAPE_NOTIFICATION, true)
      }

  override suspend fun setTapeNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putBoolean(KEY_TAPE_NOTIFICATION, enabled) }
      }

  override suspend fun listenForTapeNotificationChanged(
      onChange: (Boolean) -> Unit
  ): PreferenceListener =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.onChange(KEY_TAPE_NOTIFICATION) {
          val enabled = isTapeNotificationEnabled()
          onChange(enabled)
        }
      }

  override suspend fun isBigMoverNotificationEnabled(): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getBoolean(KEY_BIG_MOVER_NOTIFICATION, true)
      }

  override suspend fun setBigMoverNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putBoolean(KEY_BIG_MOVER_NOTIFICATION, enabled) }
      }

  override suspend fun listenForBigMoverNotificationChanged(
      onChange: (Boolean) -> Unit
  ): PreferenceListener =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.onChange(KEY_BIG_MOVER_NOTIFICATION) {
          val enabled = isBigMoverNotificationEnabled()
          onChange(enabled)
        }
      }

  companion object {
    private const val KEY_TAPE_NOTIFICATION = "key_tape_notification_v1"
    private const val KEY_BIG_MOVER_NOTIFICATION = "key_big_mover_notification_v1"
  }
}
