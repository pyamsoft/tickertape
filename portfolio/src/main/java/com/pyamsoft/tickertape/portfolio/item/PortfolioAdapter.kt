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
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioHeaderBinding
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioItemBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

class PortfolioAdapter
private constructor(
    private val factory: PortfolioItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<PortfolioItemViewState, BasePortfolioItemViewHolder<*>>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

    private const val TYPE_HEADER = 0
    private const val TYPE_HOLDING = 1

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
            if (oldItem is PortfolioItemViewState.Header) {
              return newItem is PortfolioItemViewState.Header
            }

            if (oldItem is PortfolioItemViewState.Holding) {
              if (newItem is PortfolioItemViewState.Holding) {
                return oldItem.stock.holding.id() == newItem.stock.holding.id()
              }
            }

            return false
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
    return when (val state = getItem(position)) {
      is PortfolioItemViewState.Header -> "TOP"
      is PortfolioItemViewState.Holding -> state.stock.holding.symbol().symbol()
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is PortfolioItemViewState.Header -> TYPE_HEADER
      is PortfolioItemViewState.Holding -> TYPE_HOLDING
    }
  }

  override fun getItemId(position: Int): Long {
    return when (val state = getItem(position)) {
      is PortfolioItemViewState.Header -> 0
      is PortfolioItemViewState.Holding -> state.stock.holding.id().hashCode().toLong()
    }
  }

  override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int
  ): BasePortfolioItemViewHolder<*> {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      TYPE_HOLDING -> {
        val binding = PortfolioItemBinding.inflate(inflater, parent, false)
        PortfolioItemViewHolder(binding, factory, owner, callback)
      }
      TYPE_HEADER -> {
        val binding = PortfolioHeaderBinding.inflate(inflater, parent, false)
        PortfolioItemViewHeader(binding, factory)
      }
      else ->
          throw IllegalArgumentException(
              "Cannot create ViewHolder with invalid viewType: $viewType")
    }
  }

  override fun onBindViewHolder(holder: BasePortfolioItemViewHolder<*>, position: Int) {
    val state = getItem(position)
    when (holder) {
      is PortfolioItemViewHolder -> holder.bindState(state as PortfolioItemViewState.Holding)
      is PortfolioItemViewHeader -> holder.bindState(state as PortfolioItemViewState.Header)
      else -> throw IllegalArgumentException("Cannot bind ViewHolder with invalid viewType $holder")
    }
  }

  interface Callback {

    fun onSelect(index: Int)

    fun onRemove(index: Int)
  }
}
