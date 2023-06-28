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

package com.pyamsoft.tickertape

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.main.MainComponent
import timber.log.Timber

internal object ObjectGraph {

  internal object ApplicationScope {

    private val trackingMap = mutableMapOf<Application, TickerComponent>()

    fun install(
        application: Application,
        component: TickerComponent,
    ) {
      trackingMap[application] = component
      Timber.d("Track ApplicationScoped install: $application $component")
    }

    @CheckResult
    fun retrieve(context: Context): TickerComponent {
      return retrieve(context.applicationContext as Application)
    }

    @CheckResult
    fun retrieve(activity: Activity): TickerComponent {
      return retrieve(activity.application)
    }

    @CheckResult
    fun retrieve(service: Service): TickerComponent {
      return retrieve(service.application)
    }

    @CheckResult
    fun retrieve(application: Application): TickerComponent {
      return trackingMap[application].requireNotNull {
        "Could not find ApplicationScoped internals for Application: $application"
      }
    }
  }

  internal object ActivityScope {

    private val trackingMap = mutableMapOf<ComponentActivity, MainComponent>()

    fun install(
        activity: ComponentActivity,
        component: MainComponent,
    ) {
      trackingMap[activity] = component
      Timber.d("Track ActivityScoped install: $activity $component")

      activity.doOnDestroy {
        Timber.d("Remove ActivityScoped graph onDestroy")
        trackingMap.remove(activity)
      }
    }

    @CheckResult
    fun retrieve(activity: ComponentActivity): MainComponent {
      return trackingMap[activity].requireNotNull {
        "Could not find ActivityScoped internals for Activity: $activity"
      }
    }
  }
}
