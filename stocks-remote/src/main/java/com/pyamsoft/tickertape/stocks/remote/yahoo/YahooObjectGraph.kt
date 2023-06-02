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

package com.pyamsoft.tickertape.stocks.remote.yahoo

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import timber.log.Timber

object YahooObjectGraph {

  private val trackingMap = mutableMapOf<Application, YahooComponent>()

  fun install(
      application: Application,
      component: YahooComponent,
  ) {
    trackingMap[application] = component
    Timber.d("Track YahooScoped install: $application $component")
  }

  @CheckResult
  fun retrieve(context: Context): YahooComponent {
    return retrieve(context.applicationContext as Application)
  }

  @CheckResult
  fun retrieve(activity: Activity): YahooComponent {
    return retrieve(activity.application)
  }

  @CheckResult
  fun retrieve(service: Service): YahooComponent {
    return retrieve(service.application)
  }

  @CheckResult
  fun retrieve(application: Application): YahooComponent {
    return trackingMap[application].requireNotNull {
      "Could not find YahooScoped internals for Application: $application"
    }
  }
}
