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

package com.pyamsoft.tickertape.portfolio.manage.positions

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import com.pyamsoft.pydroid.util.doOnDestroy
import javax.inject.Inject

class PositionItemViewHolder
internal constructor(
    binding: ListitemFrameBinding,
    factory: PositionItemComponent.Factory,
    owner: LifecycleOwner,
    callback: PositionItemAdapter.Callback
) : RecyclerView.ViewHolder(binding.root), ViewBinder<PositionItemViewState> {

  @Inject @JvmField internal var position: PositionItemView? = null

  private val viewBinder: ViewBinder<PositionItemViewState>

  init {
    factory.create(binding.listitemFrame).inject(this)

    val position = requireNotNull(position)

    viewBinder =
        createViewBinder(position) {
          return@createViewBinder when (it) {
            is PositionItemViewEvent.Remove -> callback.onRemove(bindingAdapterPosition)
          }
        }

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: PositionItemViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    position = null
  }
}
