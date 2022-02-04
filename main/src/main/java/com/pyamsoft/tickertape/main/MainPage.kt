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

package com.pyamsoft.tickertape.main

import android.os.Bundle
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.ui.navigator.Navigator

sealed class MainPage(val name: String) {
  object Home : MainPage("Home")
  object WatchList : MainPage("Watch List")
  object Portfolio : MainPage("Portfolio")

  @CheckResult
  fun asScreen(): Navigator.Screen<MainPage> {
    val self = this
    return object : Navigator.Screen<MainPage> {
      override val arguments: Bundle? = null
      override val screen: MainPage = self
    }
  }
}
