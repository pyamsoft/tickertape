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

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.notification.databinding.NotificationItemWrapperBinding
import com.pyamsoft.tickertape.notification.item.NotificationAdapter
import com.pyamsoft.tickertape.notification.item.NotificationItemComponent
import com.pyamsoft.tickertape.notification.item.NotificationItemViewState
import javax.inject.Inject

class TapeViewHolder
internal constructor(
    binding: NotificationItemWrapperBinding,
    factory: NotificationItemComponent.Factory,
    owner: LifecycleOwner,
    callback: NotificationAdapter.TapeCallback
) : RecyclerView.ViewHolder(binding.root), ViewBinder<NotificationItemViewState.Tape> {

  private val viewBinder: ViewBinder<NotificationItemViewState.Tape>

  @Inject @JvmField internal var toplevel: TapeTopLevel? = null

  init {
    factory.create(binding.notificationItemWrapper).inject(this)

    viewBinder = createViewBinder(toplevel.requireNotNull()) { callback.onTapeUpdated(it.enabled) }

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: NotificationItemViewState.Tape) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    toplevel = null
  }
}
