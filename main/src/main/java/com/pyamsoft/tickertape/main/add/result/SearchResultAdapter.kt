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

package com.pyamsoft.tickertape.main.add.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.pydroid.ui.databinding.ListitemLinearHorizontalBinding

class SearchResultAdapter
private constructor(
    private val factory: SearchResultComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<SearchResultViewState, SearchResultViewHolder>(DIFFER) {

  init {
    setHasStableIds(true)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        factory: SearchResultComponent.Factory,
        owner: LifecycleOwner,
        callback: Callback
    ): SearchResultAdapter {
      return SearchResultAdapter(factory, owner, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<SearchResultViewState>() {
          override fun areItemsTheSame(
              oldItem: SearchResultViewState,
              newItem: SearchResultViewState
          ): Boolean {
            return oldItem.result.symbol() == newItem.result.symbol()
          }

          override fun areContentsTheSame(
              oldItem: SearchResultViewState,
              newItem: SearchResultViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).result.symbol().symbol().hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ListitemLinearHorizontalBinding.inflate(inflater, parent, false)
    return SearchResultViewHolder(binding.listitemLinearH, factory, owner, callback)
  }

  override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }

  interface Callback {

    fun onSelect(index: Int)
  }
}
