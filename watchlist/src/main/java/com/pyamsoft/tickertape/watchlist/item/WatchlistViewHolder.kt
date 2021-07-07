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

package com.pyamsoft.tickertape.watchlist.item

import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistItemBinding
import javax.inject.Inject

class WatchlistViewHolder
internal constructor(
  binding: WatchlistItemBinding,
  factory: WatchlistItemComponent.Factory,
  owner: LifecycleOwner,
  callback: WatchlistItemAdapter.Callback
) : RecyclerView.ViewHolder(binding.root), ViewBinder<WatchlistItemViewState> {

  @Inject @JvmField internal var summary: WatchlistItemSummary? = null

  @Inject @JvmField internal var click: WatchlistItemClick? = null

  @Inject @JvmField internal var quote: WatchlistItemQuote? = null

  private val viewBinder: ViewBinder<WatchlistItemViewState>

  init {
    factory.create(binding.watchlistItem).inject(this)

    val quote = requireNotNull(quote)
    val summary = requireNotNull(summary)

    viewBinder =
        createViewBinder(quote, summary, requireNotNull(click)) {
          return@createViewBinder when (it) {
            is WatchlistItemViewEvent.Remove -> callback.onRemove(bindingAdapterPosition)
            is WatchlistItemViewEvent.Select -> callback.onSelect(bindingAdapterPosition)
          }
        }

    binding.watchlistItem.layout {
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

  override fun bindState(state: WatchlistItemViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    quote = null
    summary = null
    click = null
  }
}
