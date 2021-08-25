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
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioItemBinding
import kotlin.random.Random
import me.zhanghai.android.fastscroll.PopupTextProvider

class PortfolioAdapter
private constructor(
    private val factory: PortfolioItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<PortfolioItemViewState, RecyclerView.ViewHolder>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

    private val HEADER_HASH_CODE = Random.nextInt()
    private const val VIEW_TYPE_HEADER = 2
    private const val VIEW_TYPE_ITEM = 3

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
            return when (oldItem) {
              is PortfolioItemViewState.Header -> newItem is PortfolioItemViewState.Header
              is PortfolioItemViewState.Item ->
                  when (newItem) {
                    is PortfolioItemViewState.Item ->
                        oldItem.stock.holding.id() == newItem.stock.holding.id()
                    else -> false
                  }
            }
          }

          override fun areContentsTheSame(
              oldItem: PortfolioItemViewState,
              newItem: PortfolioItemViewState
          ): Boolean {
            return when (oldItem) {
              is PortfolioItemViewState.Header ->
                  when (newItem) {
                    is PortfolioItemViewState.Header -> oldItem == newItem
                    else -> false
                  }
              is PortfolioItemViewState.Item ->
                  when (newItem) {
                    is PortfolioItemViewState.Item -> oldItem == newItem
                    else -> false
                  }
            }
          }
        }
  }

  override fun getPopupText(position: Int): String {
    return when (val state = getItem(position)) {
      is PortfolioItemViewState.Header -> "PORTFOLIO"
      is PortfolioItemViewState.Item -> state.stock.holding.symbol().symbol()
    }
  }

  override fun getItemId(position: Int): Long {
    return when (val state = getItem(position)) {
      is PortfolioItemViewState.Header -> HEADER_HASH_CODE
      is PortfolioItemViewState.Item -> state.stock.holding.id().hashCode()
    }.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is PortfolioItemViewState.Header -> VIEW_TYPE_HEADER
      is PortfolioItemViewState.Item -> VIEW_TYPE_ITEM
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      VIEW_TYPE_ITEM -> {
        val binding = PortfolioItemBinding.inflate(inflater, parent, false)
        PortfolioItemViewHolder(binding, factory, owner, callback)
      }
      VIEW_TYPE_HEADER -> {
        val binding = ListitemFrameBinding.inflate(inflater, parent, false)
        PortfolioHeaderViewHolder(binding, factory, owner)
      }
      else -> throw AssertionError("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    return when (val state = getItem(position)) {
      is PortfolioItemViewState.Header -> {
        val viewHolder = holder as PortfolioHeaderViewHolder
        viewHolder.bindState(state)
      }
      is PortfolioItemViewState.Item -> {
        val viewHolder = holder as PortfolioItemViewHolder
        viewHolder.bindState(state)
      }
    }
  }

  interface Callback {

    fun onSelect(index: Int)

    fun onRemove(index: Int)
  }
}
