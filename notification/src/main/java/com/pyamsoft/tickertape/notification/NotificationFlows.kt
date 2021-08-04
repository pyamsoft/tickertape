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

package com.pyamsoft.tickertape.notification

import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState

data class NotificationViewState
internal constructor(
    val bottomOffset: Int,
    internal val isTapeEnabled: Boolean,
    internal val isBigMoverEnabled: Boolean,
) : UiViewState {

  sealed class ListItem {
    data class Tape internal constructor(val enabled: Boolean) : ListItem()

    data class BigMover internal constructor(val enabled: Boolean) : ListItem()

    object Spacer : ListItem()
  }

  val list =
      listOf(
          ListItem.Spacer,
          ListItem.Tape(isTapeEnabled),
          ListItem.BigMover(isBigMoverEnabled),
      )
}

sealed class NotificationViewEvent : UiViewEvent {

  data class UpdateTape internal constructor(val enabled: Boolean) : NotificationViewEvent()

  data class UpdateBigMover internal constructor(val enabled: Boolean) : NotificationViewEvent()
}
