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

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.quote.QuoteViewEvent
import com.pyamsoft.tickertape.quote.QuoteViewState
import com.pyamsoft.tickertape.watchlist.WatchlistListComponent
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistItemBinding
import javax.inject.Inject

class WatchlistViewHolder
internal constructor(
    binding: WatchlistItemBinding,
    factory: WatchlistListComponent.Factory,
    owner: LifecycleOwner,
    callback: WatchlistAdapter.Callback
) : RecyclerView.ViewHolder(binding.root), ViewBinder<QuoteViewState> {

  @Inject @JvmField internal var quote: WatchlistQuote? = null

  private val viewBinder: ViewBinder<QuoteViewState>

  init {
    factory.create(binding.watchlistItem).inject(this)

    val quote = requireNotNull(quote)

    viewBinder =
        createViewBinder(quote) {
          return@createViewBinder when (it) {
            is QuoteViewEvent.Remove -> callback.onRemove(bindingAdapterPosition)
            is QuoteViewEvent.Select -> callback.onSelect(bindingAdapterPosition)
          }
        }

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: QuoteViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    quote = null
  }
}