/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tickertape.worker.workmanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.worker.work.BgWorker
import com.pyamsoft.tickertape.worker.work.bigmover.BigMoverWork
import com.pyamsoft.tickertape.worker.workmanager.WorkerComponent
import javax.inject.Inject

class BigMoverWorker
internal constructor(
    context: Context,
    params: WorkerParameters,
) : AbstractJobWorker(context, params) {

  @Inject @JvmField internal var work: BigMoverWork? = null

  override fun onInject(component: WorkerComponent) {
    component.inject(this)
  }

  override fun worker(): BgWorker {
    return work.requireNotNull()
  }

  override fun onDestroy() {
    work = null
  }
}
