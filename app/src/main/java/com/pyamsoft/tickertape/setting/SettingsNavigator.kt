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

package com.pyamsoft.tickertape.setting

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.pyamsoft.pydroid.ui.navigator.FragmentNavigator
import com.pyamsoft.pydroid.ui.navigator.Navigator
import javax.inject.Inject
import javax.inject.Named

internal class SettingsNavigator
@Inject
internal constructor(
    dialog: SettingsDialog,
    @IdRes @Named("settings_container") fragmentContainerId: Int,
) :
    FragmentNavigator<SettingsPage>(
        lifecycleOwner = { dialog.viewLifecycleOwner },
        fragmentManager = { dialog.childFragmentManager },
        fragmentContainerId = fragmentContainerId,
    ) {

  override fun performFragmentTransaction(
      container: Int,
      data: FragmentTag,
      newScreen: Navigator.Screen<SettingsPage>,
      previousScreen: SettingsPage?
  ) {
    commitNow {
      decideAnimationForPage(newScreen.screen, previousScreen)
      replace(container, data.fragment(newScreen.arguments), data.tag)
    }
  }

  override fun provideFragmentTagMap(): Map<SettingsPage, FragmentTag> {
    return mapOf(
        SettingsPage.Settings to createFragmentTag("AppSettings") { AppSettings.newInstance() },
    )
  }

  companion object {

    private fun FragmentTransaction.decideAnimationForPage(
        newPage: SettingsPage,
        oldPage: SettingsPage?
    ) {
      // TODO
    }

    @JvmStatic
    @CheckResult
    private inline fun createFragmentTag(
        tag: String,
        crossinline fragment: (arguments: Bundle?) -> Fragment,
    ): FragmentTag {
      return object : FragmentTag {
        override val fragment: (arguments: Bundle?) -> Fragment = { fragment(it) }
        override val tag: String = tag
      }
    }
  }
}
