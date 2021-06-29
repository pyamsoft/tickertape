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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioItemBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

class PortfolioAdapter
private constructor(
    private val factory: PortfolioItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<PortfolioItemViewState, PortfolioItemViewHolder>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        factory: PortfolioItemComponent.Factory,
        owner: LifecycleOwner,
        callback: Callback
    ): PortfolioAdapter {
      return PortfolioAdapter(factory, owner, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<PortfolioItemViewState>() {

          override fun areItemsTheSame(
              oldItem: PortfolioItemViewState,
              newItem: PortfolioItemViewState
          ): Boolean {
            return oldItem.stock.holding.id() == newItem.stock.holding.id()
          }

          override fun areContentsTheSame(
              oldItem: PortfolioItemViewState,
              newItem: PortfolioItemViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getPopupText(position: Int): String {
    return getItem(position).stock.holding.symbol().symbol()
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).stock.holding.id().hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortfolioItemViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = PortfolioItemBinding.inflate(inflater, parent, false)
    return PortfolioItemViewHolder(binding, factory, owner, callback)
  }

  override fun onBindViewHolder(holder: PortfolioItemViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }

  interface Callback {

    fun onSelect(index: Int)

    fun onRemove(index: Int)
  }
}
