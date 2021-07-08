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

package com.pyamsoft.tickertape.home.index

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.tickertape.home.databinding.HomeIndexItemBinding

class HomeIndexAdapter
private constructor(
    private val factory: HomeIndexComponent.Factory,
    private val owner: LifecycleOwner,
) : ListAdapter<HomeIndexViewState, HomeIndexViewHolder>(DIFFER) {

  init {
    setHasStableIds(true)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        factory: HomeIndexComponent.Factory,
        owner: LifecycleOwner,
    ): HomeIndexAdapter {
      return HomeIndexAdapter(factory, owner)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<HomeIndexViewState>() {
          override fun areItemsTheSame(
              oldItem: HomeIndexViewState,
              newItem: HomeIndexViewState
          ): Boolean {
            return oldItem.symbol == newItem.symbol
          }

          override fun areContentsTheSame(
              oldItem: HomeIndexViewState,
              newItem: HomeIndexViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).symbol.hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeIndexViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = HomeIndexItemBinding.inflate(inflater, parent, false)
    return HomeIndexViewHolder(binding, factory, owner)
  }

  override fun onBindViewHolder(holder: HomeIndexViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }
}
