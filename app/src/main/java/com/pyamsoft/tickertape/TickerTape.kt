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
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.tickertape.core.PRIVACY_POLICY_URL
import com.pyamsoft.tickertape.core.TERMS_CONDITIONS_URL
import timber.log.Timber

class TickerTape : Application() {

  private val component by lazy {
    val url = "https://github.com/pyamsoft/tickertape"
    val parameters =
        PYDroid.Parameters(
            url, "$url/issues", PRIVACY_POLICY_URL, TERMS_CONDITIONS_URL, BuildConfig.VERSION_CODE)

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
    component.also { Timber.d("Component injected: $it") }
  }

  override fun getSystemService(name: String): Any? {
    // Use component here in a weird way to guarantee the lazy is initialized.
    return component.run { PYDroid.getSystemService(name) } ?: fallbackGetSystemService(name)
  }

  @CheckResult
  private fun fallbackGetSystemService(name: String): Any? {
    return if (name == TickerComponent::class.java.name) component
    else {
      super.getSystemService(name)
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
          "Dagger",
          "https://github.com/google/dagger",
          "A fast dependency injector for Android and Java.")
    }
  }
}
