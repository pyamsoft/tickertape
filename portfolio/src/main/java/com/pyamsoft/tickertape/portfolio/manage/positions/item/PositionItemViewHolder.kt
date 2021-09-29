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

package com.pyamsoft.tickertape.portfolio.manage.positions.item

import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.tickertape.portfolio.databinding.PositionItemHolderBinding
import com.pyamsoft.tickertape.portfolio.manage.positions.PositionsAdapter
import javax.inject.Inject

class PositionItemViewHolder
internal constructor(
    binding: PositionItemHolderBinding,
    factory: PositionItemComponent.Factory,
    owner: LifecycleOwner,
    callback: PositionsAdapter.Callback
) : BasePositionItemViewHolder<PositionItemViewState.Position>(binding.root, owner) {

  @Inject @JvmField internal var position: PositionItemView? = null

  override val viewBinder: ViewBinder<PositionItemViewState.Position>

  init {
    factory.create(binding.positionItemRoot).inject(this)

    viewBinder =
        createViewBinder(position.requireNotNull()) {
          return@createViewBinder when (it) {
            is PositionItemViewEvent.Remove -> callback.onRemove(bindingAdapterPosition)
          }
        }
  }

  override fun onTeardown() {
    viewBinder.teardown()

    position = null
  }
}
