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

package com.pyamsoft.tickertape.alert.types.pricealert

import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.inject.Injector
import com.pyamsoft.tickertape.alert.AlertWorkComponent
import com.pyamsoft.tickertape.alert.WorkResult
import com.pyamsoft.tickertape.alert.base.BaseInjector
import java.util.UUID
import javax.inject.Inject

class PriceAlertInjector
private constructor(
    context: Context,
) :
    BaseInjector<PriceAlertWorkerParameters>(
        context,
    ) {

  @JvmField @Inject internal var runner: PriceAlertRunner? = null

  override suspend fun onExecute(
      context: Context,
      id: UUID,
      tags: Set<String>,
      params: PriceAlertWorkerParameters
  ): WorkResult {
    Injector.obtainFromApplication<AlertWorkComponent>(context).inject(this)

    return runner.requireNotNull().doWork(id, tags, params)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(context: Context): PriceAlertInjector {
      return PriceAlertInjector(context.applicationContext)
    }
  }
}
