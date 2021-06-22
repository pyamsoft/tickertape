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

import androidx.annotation.CheckResult
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioItemBinding
import javax.inject.Inject

class PortfolioItemViewHolder
internal constructor(
    binding: PortfolioItemBinding,
    factory: PortfolioItemComponent.Factory,
    owner: LifecycleOwner,
    callback: PortfolioAdapter.Callback
) : BasePortfolioItemViewHolder<PortfolioItemViewState.Holding>(binding.root) {

  @Inject @JvmField internal var quote: PortfolioItemQuote? = null

  @Inject @JvmField internal var summary: PortfolioItemSummary? = null

  @Inject @JvmField internal var click: PortfolioItemClick? = null

  override val viewBinder: ViewBinder<PortfolioItemViewState.Holding>

  init {
    factory.create(binding.portfolioItemRoot).inject(this)

    val quote = requireNotNull(quote)
    val summary = requireNotNull(summary)

    viewBinder =
        createViewBinder(quote, summary, requireNotNull(click)) {
          val pos = modelPosition()
          return@createViewBinder when (it) {
            is PortfolioItemViewEvent.Remove -> callback.onRemove(pos)
            is PortfolioItemViewEvent.Select -> callback.onSelect(pos)
          }
        }

    binding.portfolioItemRoot.layout {
      quote.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }

      summary.also {
        connect(it.id(), ConstraintSet.TOP, quote.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainHeight(it.id(), ConstraintSet.WRAP_CONTENT)
      }
    }

    owner.doOnDestroy { teardown() }
  }

  @CheckResult
  private fun modelPosition(): Int {
    // Subtract 1 because item 0 is the header
    return bindingAdapterPosition - 1
  }

  override fun onTeardown() {
    click = null
    quote = null
    summary = null
  }
}
