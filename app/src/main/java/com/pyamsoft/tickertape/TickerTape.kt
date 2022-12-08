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

package com.pyamsoft.tickertape

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.installPYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.tickertape.alert.AlarmFactory
import com.pyamsoft.tickertape.alert.AlertWorkComponent
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.initOnAppStart
import com.pyamsoft.tickertape.core.PRIVACY_POLICY_URL
import com.pyamsoft.tickertape.core.TERMS_CONDITIONS_URL
import com.pyamsoft.tickertape.receiver.BootReceiver
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

class TickerTape : Application() {

  private var component: TickerComponent? = null

  @Inject @JvmField internal var alerter: Alerter? = null

  @Inject @JvmField internal var alarmFactory: AlarmFactory? = null

  @CheckResult
  private fun installPYDroid(): ModuleProvider {
    val url = "https://github.com/pyamsoft/tickertape"

    return installPYDroid(
        PYDroid.Parameters(
            viewSourceUrl = url,
            bugReportUrl = "$url/issues",
            privacyPolicyUrl = PRIVACY_POLICY_URL,
            termsConditionsUrl = TERMS_CONDITIONS_URL,
            version = BuildConfig.VERSION_CODE,
            logger = createLogger(),
            theme = TickerTapeThemeProvider,
            debug =
                PYDroid.DebugParameters(
                    enabled = true,
                    upgradeAvailable = true,
                    ratingAvailable = false,
                ),
        ),
    )
  }

  private fun installComponent(moduleProvider: ModuleProvider) {
    if (component == null) {
      val mods = moduleProvider.get()
      component =
          DaggerTickerComponent.factory()
              .create(
                  application = this,
                  debug = isDebugMode(),
                  imageLoader = mods.imageLoader(),
                  theming = mods.theming(),
              )
    } else {
      Timber.w("Cannot install TickerComponent again")
    }
  }

  private fun beginWork() {
    // Coroutine start up is slow. What we can do instead is create a handler, which is cheap, and
    // post to the main thread to defer this work until after start up is done
    Handler(Looper.getMainLooper()).post {
      MainScope().launch(context = Dispatchers.Default) {
        alerter.requireNotNull().initOnAppStart(alarmFactory.requireNotNull())
      }
    }
  }

  @CheckResult
  private fun componentGraph(): TickerComponent {
    return component.requireNotNull { "TickerComponent was not installed, something is wrong." }
  }

  @CheckResult
  private fun provideModuleDependencies(name: String): Any? {
    return when (name) {
      AlertWorkComponent::class.java.name -> componentGraph().plusAlertWorkComponent()
      else -> null
    }
  }

  /** Ensure the BootReceiver is set to state enabled */
  private fun ensureBootReceiverEnabled() {
    val componentName = ComponentName(this, BootReceiver::class.java)
    packageManager.setComponentEnabledSetting(
        componentName,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP,
    )
  }

  override fun onCreate() {
    super.onCreate()

    installLogger()
    val modules = installPYDroid()
    installComponent(modules)

    componentGraph().inject(this)
    addLibraries()
    ensureBootReceiverEnabled()
    beginWork()
  }

  override fun getSystemService(name: String): Any? {
    return if (name == TickerComponent::class.java.name) componentGraph()
    else provideModuleDependencies(name) ?: super.getSystemService(name)
  }

  companion object {

    @JvmStatic
    private fun addLibraries() {
      // We are using pydroid-notify
      OssLibraries.usingNotify = true

      // We are using pydroid-autopsy
      OssLibraries.usingAutopsy = true

      // We are using pydroid-inject
      OssLibraries.usingInject = true

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
      }
    }
  }
}
