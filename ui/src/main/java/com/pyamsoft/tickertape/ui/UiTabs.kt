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

package com.pyamsoft.tickertape.ui

import android.view.LayoutInflater
import androidx.annotation.CheckResult
import com.google.android.material.tabs.TabLayout
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.tickertape.ui.databinding.UiTabsBinding
import timber.log.Timber

abstract class UiTabs<S : UiViewState, V : UiViewEvent, E : Enum<*>>
protected constructor(appBarActivity: AppBarActivity) : UiView<S, V>() {

  private var _bindingRoot: TabLayout? = null
  private val layoutRoot: TabLayout
    get() = requireNotNull(_bindingRoot)

  init {
    // Replace the app bar background during switcher presence
    doOnInflate {
      appBarActivity.requireAppBar { appBar ->
        val inflater = LayoutInflater.from(appBar.context)
        val binding = UiTabsBinding.inflate(inflater, appBar)
        _bindingRoot = binding.uiTabs.also { onCreate(it) }
      }
    }

    doOnTeardown {
      _bindingRoot?.also { tabs ->
        appBarActivity.withAppBar { appBar -> appBar.removeView(tabs) }
        onDestroy(tabs)
      }
    }
  }

  private fun onCreate(tabs: TabLayout) {
    addTabs(tabs)
    attachListener(tabs)
  }

  private fun onDestroy(tabs: TabLayout) {
    tabs.removeAllTabs()
  }

  private fun attachListener(tabs: TabLayout) {
    val listener =
        object : TabLayout.OnTabSelectedListener {

          override fun onTabSelected(tab: TabLayout.Tab) {
            handleTabSelected(tab)
          }

          override fun onTabUnselected(tab: TabLayout.Tab) {}

          override fun onTabReselected(tab: TabLayout.Tab) {}
        }

    tabs.apply {
      addOnTabSelectedListener(listener)
      doOnTeardown { removeOnTabSelectedListener(listener) }
    }
  }

  protected fun handleSection(section: E) {
    val tabs = layoutRoot
    for (i in 0 until tabs.tabCount) {
      val tab = tabs.getTabAt(i)
      if (tab == null) {
        Timber.w("No tab found at index: $i")
        continue
      }

      val tag = getTabSection(tab, section::class.java)
      if (tag == section) {
        tabs.selectTab(tab, true)
        break
      }
    }
  }

  protected abstract fun addTabs(tabs: TabLayout)

  protected abstract fun handleTabSelected(tab: TabLayout.Tab)

  companion object {

    @JvmStatic
    @CheckResult
    protected fun <E : Enum<*>> getTabSection(tab: TabLayout.Tab, clazz: Class<E>): E? {
      val tag = tab.tag
      if (tag == null) {
        Timber.w("No tag found on tab: $tab")
        return null
      }

      if (tag::class.java !== clazz) {
        Timber.w("Tag is not ${clazz.simpleName} model: $tag")
        return null
      }

      return clazz.cast(tag).requireNotNull()
    }
  }
}
