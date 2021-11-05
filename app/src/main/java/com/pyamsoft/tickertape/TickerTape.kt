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
import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult
import coil.Coil
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.tickertape.alert.Alerter
import com.pyamsoft.tickertape.alert.inject.AlertComponent
import com.pyamsoft.tickertape.alert.work.AlarmFactory
import com.pyamsoft.tickertape.core.PRIVACY_POLICY_URL
import com.pyamsoft.tickertape.core.TERMS_CONDITIONS_URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TickerTape : Application() {

  @Inject @JvmField internal var alerter: Alerter? = null

  @Inject @JvmField internal var alarmFactory: AlarmFactory? = null

  private val applicationScope by lazy(LazyThreadSafetyMode.NONE) { MainScope() }

  private val component by lazy {
    val url = "https://github.com/pyamsoft/tickertape"
    val parameters =
        PYDroid.Parameters(
            googlePlayLicenseVerificationKey = BuildConfig.LICENSE_KEY,
            viewSourceUrl = url,
            bugReportUrl = "$url/issues",
            privacyPolicyUrl = PRIVACY_POLICY_URL,
            termsConditionsUrl = TERMS_CONDITIONS_URL,
            version = BuildConfig.VERSION_CODE,
            imageLoader = { Coil.imageLoader(this) },
            logger = createLogger(),
            theme = { themeProvider, content ->
              TickerTapeTheme(
                  themeProvider = themeProvider,
                  content = content,
              )
            },
        )

    return@lazy createComponent(PYDroid.init(this, parameters))
  }

  @CheckResult
  private fun createComponent(provider: ModuleProvider): TickerComponent {
    return DaggerTickerComponent.factory()
        .create(
            this,
            isDebugMode(),
            provider.get().theming(),
            provider.get().imageLoader(),
        )
        .also { addLibraries() }
  }

  override fun onCreate() {
    super.onCreate()
    component.inject(this)
    beginWork()
  }

  private fun beginWork() {
    // Coroutine start up is slow. What we can do instead is create a handler, which is cheap, and
    // post to the main thread to defer this work until after start up is done
    Handler(Looper.getMainLooper()).post {
      applicationScope.launch(context = Dispatchers.Default) {
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
        AlertComponent::class.java.name -> plusAlertComponent()
        else -> null
      }
    }
  }

  companion object {

    @JvmStatic
    private fun addLibraries() {
      // We are using pydroid-notify
      OssLibraries.usingNotify = true

      // We are using pydroid-autopsy
      OssLibraries.usingAutopsy = true

      OssLibraries.add(
          "Room",
          "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room/",
          "The AndroidX Jetpack Room library. Fluent SQLite database access.")
      OssLibraries.add(
          "WorkManager",
          "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/work/",
          "The AndroidX Jetpack WorkManager library. Schedule periodic work in a device friendly way.")
      OssLibraries.add(
          "Dagger",
          "https://github.com/google/dagger",
          "A fast dependency injector for Android and Java.")
    }
  }
}
