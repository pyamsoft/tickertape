/*
 * Copyright 2023 pyamsoft
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
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.util.booleanFlow
import com.pyamsoft.tickertape.worker.work.bigmover.BigMoverPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    enforcer: ThreadEnforcer,
    context: Context,
) : BigMoverPreferences {

  private val preferences by lazy {
    enforcer.assertOffMainThread()
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }

  override suspend fun setBigMoverNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        preferences.edit { putBoolean(BigMovers.KEY_NOTIFICATION_ENABLED, enabled) }
      }

  override suspend fun listenForBigMoverNotificationChanged(): Flow<Boolean> =
      withContext(context = Dispatchers.IO) {
        preferences.booleanFlow(
            BigMovers.KEY_NOTIFICATION_ENABLED,
            BigMoverPreferences.VALUE_DEFAULT_NOTIFICATION_ENABLED,
        )
      }

  private object BigMovers {

    const val KEY_NOTIFICATION_ENABLED = "key_big_mover_notification_v1"
  }
}
