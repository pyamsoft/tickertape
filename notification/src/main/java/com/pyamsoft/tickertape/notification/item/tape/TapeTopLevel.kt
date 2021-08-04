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

package com.pyamsoft.tickertape.notification.item.tape

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.notification.item.NotificationItemToplevel
import com.pyamsoft.tickertape.notification.item.NotificationItemViewEvent
import com.pyamsoft.tickertape.notification.item.NotificationItemViewState
import javax.inject.Inject

class TapeTopLevel @Inject internal constructor(parent: ViewGroup) :
    NotificationItemToplevel<NotificationItemViewState.Tape, NotificationItemViewEvent.UpdateTape>(
        parent) {

  override fun provideToggleChangedEvent(isChecked: Boolean): NotificationItemViewEvent.UpdateTape {
    return NotificationItemViewEvent.UpdateTape(enabled = isChecked)
  }

  override fun provideTitle(state: UiRender<NotificationItemViewState.Tape>): UiRender<String> {
    return state.mapChanged { it.title }
  }

  override fun provideEnabled(state: UiRender<NotificationItemViewState.Tape>): UiRender<Boolean> {
    return state.mapChanged { it.enabled }
  }
}
