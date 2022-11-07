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
import com.pyamsoft.pydroid.util.booleanFlow
import com.pyamsoft.pydroid.util.intFlow
import com.pyamsoft.tickertape.alert.types.bigmover.BigMoverPreferences
import com.pyamsoft.tickertape.tape.TapePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    context: Context,
) : TapePreferences, BigMoverPreferences {

  private val preferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }

  override suspend fun setTapeNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putBoolean(Tape.KEY_NOTIFICATION_ENABLED, enabled) }
      }

  override suspend fun setTapePageSize(size: Int) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putInt(Tape.KEY_PAGE_SIZE, size) }
      }

  override suspend fun listenForTapePageSizeChanged(): Flow<Int> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.intFlow(
            Tape.KEY_PAGE_SIZE,
            TapePreferences.VALUE_DEFAULT_PAGE_SIZE,
        )
      }

  override suspend fun listenForTapeNotificationChanged(): Flow<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.booleanFlow(
            Tape.KEY_NOTIFICATION_ENABLED,
            TapePreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED,
        )
      }

  override suspend fun setBigMoverNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putBoolean(BigMovers.KEY_NOTIFICATION_ENABLED, enabled) }
      }

  override suspend fun listenForBigMoverNotificationChanged(): Flow<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.booleanFlow(
            BigMovers.KEY_NOTIFICATION_ENABLED,
            BigMoverPreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED,
        )
      }

  private object BigMovers {

    const val KEY_NOTIFICATION_ENABLED = "key_big_mover_notification_v1"
  }

  private object Tape {

    const val KEY_NOTIFICATION_ENABLED = "key_tape_notification_v1"

    const val KEY_PAGE_SIZE = "key_tape_page_size_v1"
  }
}
