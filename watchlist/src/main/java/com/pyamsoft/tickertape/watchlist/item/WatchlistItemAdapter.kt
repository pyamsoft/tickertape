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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.tickertape.watchlist.WatchlistListComponent
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistItemBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

class WatchlistItemAdapter
private constructor(
    private val factory: WatchlistListComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<WatchlistItemViewState, WatchlistViewHolder>(DIFFER), PopupTextProvider {

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        factory: WatchlistListComponent.Factory,
        owner: LifecycleOwner,
        callback: Callback
    ): WatchlistItemAdapter {
      return WatchlistItemAdapter(factory, owner, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<WatchlistItemViewState>() {
          override fun areItemsTheSame(
              oldItem: WatchlistItemViewState,
              newItem: WatchlistItemViewState
          ): Boolean {
            return oldItem.symbol.symbol() == newItem.symbol.symbol()
          }

          override fun areContentsTheSame(
              oldItem: WatchlistItemViewState,
              newItem: WatchlistItemViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getPopupText(position: Int): String {
    val state = getItem(position)
    return state.symbol.symbol()
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).symbol.symbol().hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = WatchlistItemBinding.inflate(inflater, parent, false)
    return WatchlistViewHolder(binding, factory, owner, callback)
  }

  override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }

  interface Callback {

    fun onSelect(index: Int)

    fun onRemove(index: Int)
  }
}
