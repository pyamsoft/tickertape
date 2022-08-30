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
import com.pyamsoft.pydroid.util.stringFlow
import com.pyamsoft.tickertape.alert.preference.BigMoverPreferences
import com.pyamsoft.tickertape.quote.QuotePreferences
import com.pyamsoft.tickertape.quote.QuoteSort
import com.pyamsoft.tickertape.tape.TapePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    context: Context,
) : TapePreferences, BigMoverPreferences, QuotePreferences {

  private val preferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }

  override suspend fun setTapeNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putBoolean(KEY_TAPE_NOTIFICATION, enabled) }
      }

  override suspend fun listenForTapeNotificationChanged(): Flow<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.booleanFlow(KEY_TAPE_NOTIFICATION, true)
      }

  override suspend fun setBigMoverNotificationEnabled(enabled: Boolean) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        preferences.edit { putBoolean(KEY_BIG_MOVER_NOTIFICATION, enabled) }
      }

  override suspend fun listenForBigMoverNotificationChanged(): Flow<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.booleanFlow(KEY_BIG_MOVER_NOTIFICATION, true)
      }

  override suspend fun setQuoteSort(sort: QuoteSort) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.edit { putString(KEY_QUOTE_SORT, sort.name) }
      }

  override suspend fun listenForQuoteSortChanged(): Flow<QuoteSort> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences
            .stringFlow(KEY_BIG_MOVER_NOTIFICATION, QuoteSort.REGULAR.name)
            .map { QuoteSort.valueOf(it) }
      }

  companion object {
    private const val KEY_QUOTE_SORT = "key_quote_sort_1"
    private const val KEY_TAPE_NOTIFICATION = "key_tape_notification_v1"
    private const val KEY_BIG_MOVER_NOTIFICATION = "key_big_mover_notification_v1"
  }
}
