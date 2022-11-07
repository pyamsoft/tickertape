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
import coil.ImageLoader
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.initOnAppStart
import com.pyamsoft.tickertape.alert.AlertWorkComponent
import com.pyamsoft.tickertape.alert.AlarmFactory
import com.pyamsoft.tickertape.core.PRIVACY_POLICY_URL
import com.pyamsoft.tickertape.core.TERMS_CONDITIONS_URL
import com.pyamsoft.tickertape.receiver.BootReceiver
import com.pyamsoft.tickertape.ui.TickerTapeTheme
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TickerTape : Application() {

  @Inject @JvmField internal var alerter: Alerter? = null

  @Inject @JvmField internal var alarmFactory: AlarmFactory? = null

  private val component by lazy {
    val url = "https://github.com/pyamsoft/tickertape"

    val lazyImageLoader = lazy(LazyThreadSafetyMode.NONE) { ImageLoader(this) }

    installLogger()

    val parameters =
        PYDroid.Parameters(
            // Must be lazy since Coil calls getSystemService() internally, leading to SO exception
            lazyImageLoader = lazyImageLoader,
            viewSourceUrl = url,
            bugReportUrl = "$url/issues",
            privacyPolicyUrl = PRIVACY_POLICY_URL,
            termsConditionsUrl = TERMS_CONDITIONS_URL,
            version = BuildConfig.VERSION_CODE,
            logger = createLogger(),
            theme = { activity, themeProvider, content ->
              activity.TickerTapeTheme(
                  themeProvider = themeProvider,
                  content = content,
              )
            },
        )

    return@lazy createComponent(PYDroid.init(this, parameters), lazyImageLoader)
  }

  @CheckResult
  private fun createComponent(
      provider: ModuleProvider,
      lazyImageLoader: Lazy<ImageLoader>,
  ): TickerComponent {
    return DaggerTickerComponent.factory()
        .create(
            application = this,
            debug = isDebugMode(),
            lazyImageLoader = lazyImageLoader,
            theming = provider.get().theming(),
        )
        .also { addLibraries() }
  }

  override fun onCreate() {
    super.onCreate()
    component.inject(this)
    ensureBootReceiverEnabled()
    beginWork()
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

  override fun getSystemService(name: String): Any? {
    // Use component here in a weird way to guarantee the lazy is initialized.
    return component.run { PYDroid.getSystemService(name) } ?: fallbackGetSystemService(name)
  }

  @CheckResult
  private fun fallbackGetSystemService(name: String): Any? {
    return if (name == TickerComponent::class.java.name) component
    else {
      provideModuleDependencies(name) ?: super.getSystemService(name)
    }
  }

  @CheckResult
  private fun provideModuleDependencies(name: String): Any? {
    return component.run {
      when (name) {
        AlertWorkComponent::class.java.name -> plusAlertWorkComponent()
        else -> null
      }
    }
  }

  /** Ensure the BootReceiver is set to state enabled */
  private fun ensureBootReceiverEnabled() {
    val component = ComponentName(this, BootReceiver::class.java)
    packageManager.setComponentEnabledSetting(
        component,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP,
    )
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
