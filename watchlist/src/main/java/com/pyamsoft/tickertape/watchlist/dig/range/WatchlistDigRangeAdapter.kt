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

package com.pyamsoft.tickertape.watchlist.dig.range

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.tickertape.watchlist.databinding.WatchlistDigRangeItemBinding

class WatchlistDigRangeAdapter
private constructor(
    private val factory: WatchlistDigRangeComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<WatchlistDigRangeViewState, WatchlistDigRangeViewHolder>(DIFFER) {

  init {
    setHasStableIds(true)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        factory: WatchlistDigRangeComponent.Factory,
        owner: LifecycleOwner,
        callback: Callback
    ): WatchlistDigRangeAdapter {
      return WatchlistDigRangeAdapter(factory, owner, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<WatchlistDigRangeViewState>() {
          override fun areItemsTheSame(
              oldItem: WatchlistDigRangeViewState,
              newItem: WatchlistDigRangeViewState
          ): Boolean {
            return oldItem.range == newItem.range
          }

          override fun areContentsTheSame(
              oldItem: WatchlistDigRangeViewState,
              newItem: WatchlistDigRangeViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).range.hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistDigRangeViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = WatchlistDigRangeItemBinding.inflate(inflater, parent, false)
    return WatchlistDigRangeViewHolder(binding, factory, owner, callback)
  }

  override fun onBindViewHolder(holder: WatchlistDigRangeViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }

  interface Callback {

    fun onSelect(index: Int)
  }
}
