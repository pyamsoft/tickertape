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

package com.pyamsoft.tickertape.notification.item

import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.notification.databinding.NotificationItemToplevelBinding

abstract class NotificationItemToplevel<
    S : NotificationItemViewState, V : NotificationItemViewEvent>
protected constructor(parent: ViewGroup) :
    BaseUiView<S, V, NotificationItemToplevelBinding>(parent) {

  final override val viewBinding = NotificationItemToplevelBinding::inflate

  final override val layoutRoot by boundView { notificationItemToplevel }

  private val toggleListener =
      CompoundButton.OnCheckedChangeListener { _, isChecked ->
        publish(provideToggleChangedEvent(isChecked))
      }

  init {
    doOnTeardown { binding.notificationItemToplevelText.text = null }

    doOnTeardown { binding.notificationItemToplevelToggle.setOnCheckedChangeListener(null) }
  }

  @CheckResult protected abstract fun provideToggleChangedEvent(isChecked: Boolean): V

  @CheckResult protected abstract fun provideTitle(state: UiRender<S>): UiRender<String>

  @CheckResult protected abstract fun provideEnabled(state: UiRender<S>): UiRender<Boolean>

  final override fun onRender(state: UiRender<S>) {
    provideTitle(state).render(viewScope) { handleTitle(it) }
    provideEnabled(state).render(viewScope) { handleEnabled(it) }
  }

  private fun handleTitle(title: String) {
    binding.notificationItemToplevelText.text = title
  }

  private fun handleEnabled(enabled: Boolean) {
    binding.notificationItemToplevelToggle.apply {
      setOnCheckedChangeListener(null)
      isChecked = enabled
      setOnCheckedChangeListener(toggleListener)
    }
  }
}
