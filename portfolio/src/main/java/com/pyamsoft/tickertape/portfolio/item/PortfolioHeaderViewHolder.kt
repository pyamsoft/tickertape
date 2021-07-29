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

package com.pyamsoft.tickertape.portfolio.item

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.portfolio.PortfolioHeader
import javax.inject.Inject

class PortfolioHeaderViewHolder
internal constructor(
    binding: ListitemFrameBinding,
    factory: PortfolioItemComponent.Factory,
    owner: LifecycleOwner,
) : RecyclerView.ViewHolder(binding.root), ViewBinder<PortfolioItemViewState.Header> {

  @Inject @JvmField internal var header: PortfolioHeader? = null

  private val viewBinder: ViewBinder<PortfolioItemViewState.Header>

  init {
    factory.create(binding.listitemFrame).inject(this)

    viewBinder = createViewBinder(header.requireNotNull()) {}

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: PortfolioItemViewState.Header) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    header = null
  }
}
