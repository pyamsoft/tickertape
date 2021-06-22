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

import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioHeaderBinding
import javax.inject.Inject

class PortfolioItemViewHeader
internal constructor(
    binding: PortfolioHeaderBinding,
    factory: PortfolioItemComponent.Factory,
) : BasePortfolioItemViewHolder<PortfolioItemViewState.Header>(binding.root) {

  override val viewBinder: ViewBinder<PortfolioItemViewState.Header>

  @Inject @JvmField internal var header: PortfolioHeader? = null

  init {
    factory.create(binding.portfolioHeader).inject(this)

    viewBinder = createViewBinder(requireNotNull(header)) {}
  }

  override fun onTeardown() {
    header = null
  }
}
