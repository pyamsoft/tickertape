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

package com.pyamsoft.tickertape

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.installPYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.tickertape.core.PRIVACY_POLICY_URL
import com.pyamsoft.tickertape.core.TERMS_CONDITIONS_URL
import com.pyamsoft.tickertape.worker.WorkerQueue
import com.pyamsoft.tickertape.worker.enqueueAppWork
import com.pyamsoft.tickertape.worker.workmanager.WorkerObjectGraph
import javax.inject.Inject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TickerTape : Application() {

  @Inject @JvmField internal var workerQueue: WorkerQueue? = null

  @CheckResult
  private fun initPYDroid(): ModuleProvider {
    val url = "https://github.com/pyamsoft/tickertape"

    return installPYDroid(
        PYDroid.Parameters(
            viewSourceUrl = url,
            bugReportUrl = "$url/issues",
            privacyPolicyUrl = PRIVACY_POLICY_URL,
            termsConditionsUrl = TERMS_CONDITIONS_URL,
            version = BuildConfig.VERSION_CODE,
            logger = createLogger(),
        ),
    )
  }

  private fun installExtraComponents(component: TickerComponent) {
    WorkerObjectGraph.install(this, component.plusWorkerComponent().create())
  }

  private fun installComponent(moduleProvider: ModuleProvider) {
    val mods = moduleProvider.get()
    val component =
        DaggerTickerComponent.factory()
            .create(
                application = this,
                debug = isDebugMode(),
                imageLoader = mods.imageLoader(),
                theming = mods.theming(),
                enforcer = mods.enforcer(),
            )
    component.inject(this)

    ObjectGraph.ApplicationScope.install(this, component)
    installExtraComponents(component)
  }

  private fun beginWork() {
    // Coroutine start up is slow. What we can do instead is create a handler, which is cheap, and
    // post to the main thread to defer this work until after start up is done
    Handler(Looper.getMainLooper()).post {
      MainScope().launch(context = Dispatchers.Default) { workerQueue?.enqueueAppWork() }
    }
  }

  override fun onCreate() {
    super.onCreate()
    val modules = initPYDroid()

    val scope =
        CoroutineScope(
            context = SupervisorJob() + Dispatchers.Default + CoroutineName(this::class.java.name),
        )
    installLogger(
        scope = scope,
        inAppDebugStatus = modules.get().inAppDebugStatus(),
    )

    installComponent(modules)
    addLibraries()
    beginWork()
  }

  companion object {

    @JvmStatic
    private fun addLibraries() {
      // We are using pydroid-notify
      OssLibraries.usingNotify = true

      // We are using pydroid-autopsy
      OssLibraries.usingAutopsy = true

      OssLibraries.apply {
        add(
            "Retrofit",
            "https://square.github.io/retrofit/",
            "Type-safe HTTP client for Android and Java by Square, Inc.",
        )
        add(
            "Moshi",
            "https://github.com/square/moshi",
            "A modern JSON library for Android and Java.",
        )
        add(
            "OkHTTP",
            "https://github.com/square/okhttp",
            "An HTTP+HTTP/2 client for Android and Java applications.",
        )
        add(
            "Cachify",
            "https://github.com/pyamsoft/cachify",
            "Simple in-memory caching of all the things",
        )

        add(
            "Room",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room/",
            "The AndroidX Jetpack Room library. Fluent SQLite database access.",
        )
        add(
            "WorkManager",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/work/",
            "The AndroidX Jetpack WorkManager library. Schedule periodic work in a device friendly way.",
        )
        add(
            "Dagger",
            "https://github.com/google/dagger",
            "A fast dependency injector for Android and Java.",
        )
        add(
            "Vico",
            "https://github.com/patrykandpatryk/vico",
            "A light and extensible chart library for Android.",
        )

        add(
            "Accompanist Pager",
            "https://google.github.io/accompanist/pager/",
            "A library which provides paging layouts for Jetpack Compose.",
        )
      }
    }
  }
}
