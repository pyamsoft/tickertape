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
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import javax.inject.Inject

class PositionHeaderViewHolder
internal constructor(
    binding: ListitemFrameBinding,
    factory: PositionItemComponent.Factory,
    owner: LifecycleOwner,
) : BasePositionItemViewHolder<PositionItemViewState.Header>(binding, owner) {

  override val viewBinder: ViewBinder<PositionItemViewState.Header>

  @Inject @JvmField internal var header: PositionHeaderView? = null

  init {
    factory.create(binding.listitemFrame).inject(this)

    viewBinder = createViewBinder(header.requireNotNull()) {}
  }

  override fun onTeardown() {}
}
