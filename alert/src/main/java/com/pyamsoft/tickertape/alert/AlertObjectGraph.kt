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

package com.pyamsoft.tickertape.alert

import android.app.Application
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import timber.log.Timber

object AlertObjectGraph {

  object WorkerScope {

    private val trackingMap = mutableMapOf<Application, AlertWorkComponent>()

    /** Called from Application */
    fun install(
        application: Application,
        component: AlertWorkComponent,
    ) {
      trackingMap[application] = component
      Timber.d("Track ApplicationScoped install: $application $component")
    }

    @CheckResult
    internal fun retrieve(context: Context): AlertWorkComponent {
      return retrieve(context.applicationContext as Application)
    }

    @CheckResult
    internal fun retrieve(application: Application): AlertWorkComponent {
      return trackingMap[application].requireNotNull {
        "Could not find ApplicationScoped internals for Application: $application"
      }
    }
  }
}
