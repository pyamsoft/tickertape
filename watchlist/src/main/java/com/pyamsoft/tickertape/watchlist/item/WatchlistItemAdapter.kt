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
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistItemBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

class WatchlistItemAdapter
private constructor(
    private val factory: WatchlistItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<WatchlistItemViewState, RecyclerView.ViewHolder>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

    private const val VIEW_TYPE_ITEM = 2

    @JvmStatic
    @CheckResult
    fun create(
        factory: WatchlistItemComponent.Factory,
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
            return when (oldItem) {
              is WatchlistItemViewState.Item ->
                  when (newItem) {
                    is WatchlistItemViewState.Item -> oldItem.symbol == newItem.symbol
                  }
            }
          }

          override fun areContentsTheSame(
              oldItem: WatchlistItemViewState,
              newItem: WatchlistItemViewState
          ): Boolean {
            return when (oldItem) {
              is WatchlistItemViewState.Item ->
                  when (newItem) {
                    is WatchlistItemViewState.Item -> oldItem == newItem
                  }
            }
          }
        }
  }

  override fun getPopupText(position: Int): String {
    return when (val state = getItem(position)) {
      is WatchlistItemViewState.Item -> state.symbol.symbol()
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is WatchlistItemViewState.Item -> VIEW_TYPE_ITEM
    }
  }

  override fun getItemId(position: Int): Long {
    return when (val state = getItem(position)) {
      is WatchlistItemViewState.Item -> state.symbol.symbol().hashCode()
    }.toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      VIEW_TYPE_ITEM -> {
        val binding = WatchlistItemBinding.inflate(inflater, parent, false)
        WatchlistViewHolder(binding, factory, owner, callback)
      }
      else -> throw AssertionError("Invalid View type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    return when (val state = getItem(position)) {
      is WatchlistItemViewState.Item -> {
        val viewHolder = holder as WatchlistViewHolder
        viewHolder.bindState(state)
      }
    }
  }

  interface Callback {

    fun onSelect(index: Int)

    fun onRemove(index: Int)
  }
}
