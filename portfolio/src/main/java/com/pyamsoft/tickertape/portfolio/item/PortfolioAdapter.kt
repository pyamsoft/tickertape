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
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import com.pyamsoft.tickertape.portfolio.databinding.PortfolioItemBinding
import com.pyamsoft.tickertape.ui.SpacerViewHolder
import me.zhanghai.android.fastscroll.PopupTextProvider
import timber.log.Timber

class PortfolioAdapter
private constructor(
    private val factory: PortfolioItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val appBarActivity: AppBarActivity,
    private val callback: Callback
) : ListAdapter<PortfolioItemViewState, RecyclerView.ViewHolder>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

    private const val VIEW_TYPE_SPACER = 1
    private const val VIEW_TYPE_HEADER = 2
    private const val VIEW_TYPE_ITEM = 3

    @JvmStatic
    @CheckResult
    fun create(
        factory: PortfolioItemComponent.Factory,
        owner: LifecycleOwner,
        appBarActivity: AppBarActivity,
        callback: Callback
    ): PortfolioAdapter {
      return PortfolioAdapter(factory, owner, appBarActivity, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<PortfolioItemViewState>() {

          override fun areItemsTheSame(
              oldItem: PortfolioItemViewState,
              newItem: PortfolioItemViewState
          ): Boolean {
            return oldItem::class.java == newItem::class.java
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
      is PortfolioItemViewState.Header -> "PORTFOLIO"
      is PortfolioItemViewState.Item -> state.stock.holding.symbol().symbol()
      is PortfolioItemViewState.Spacer -> "TOP"
    }
  }

  override fun getItemId(position: Int): Long {
    return when (val state = getItem(position)) {
      is PortfolioItemViewState.Header -> state.hashCode()
      is PortfolioItemViewState.Item -> state.stock.holding.id().hashCode()
      is PortfolioItemViewState.Spacer -> PortfolioItemViewState.Spacer.hashCode()
    }.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is PortfolioItemViewState.Header -> VIEW_TYPE_HEADER
      is PortfolioItemViewState.Item -> VIEW_TYPE_ITEM
      is PortfolioItemViewState.Spacer -> VIEW_TYPE_SPACER
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
      VIEW_TYPE_SPACER -> SpacerViewHolder.create(parent, appBarActivity, owner)
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
      is PortfolioItemViewState.Spacer -> Timber.d("Ignore bind on Spacer item")
    }
  }

  interface Callback {

    fun onSelect(index: Int)

    fun onRemove(index: Int)
  }
}
