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

package com.pyamsoft.tickertape.portfolio.manage.positions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import com.pyamsoft.tickertape.portfolio.databinding.PositionItemHolderBinding
import com.pyamsoft.tickertape.portfolio.manage.positions.item.BasePositionItemViewHolder
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionFooterViewHolder
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemComponent
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemViewHolder
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemViewState
import me.zhanghai.android.fastscroll.PopupTextProvider

class PositionsAdapter
private constructor(
    private val factory: PositionItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<PositionItemViewState, BasePositionItemViewHolder<*>>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

    private const val TYPE_POSITION = 1
    private const val TYPE_FOOTER = 2

    @JvmStatic
    @CheckResult
    fun create(
        factory: PositionItemComponent.Factory,
        owner: LifecycleOwner,
        callback: Callback
    ): PositionsAdapter {
      return PositionsAdapter(factory, owner, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<PositionItemViewState>() {
          override fun areItemsTheSame(
              oldItem: PositionItemViewState,
              newItem: PositionItemViewState
          ): Boolean {
            return oldItem::class.java == newItem::class.java
          }

          override fun areContentsTheSame(
              oldItem: PositionItemViewState,
              newItem: PositionItemViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getPopupText(position: Int): String {
    return when (val state = getItem(position)) {
      is PositionItemViewState.Footer -> "Total"
      is PositionItemViewState.Position -> state.holding.symbol().symbol()
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is PositionItemViewState.Footer -> TYPE_FOOTER
      is PositionItemViewState.Position -> TYPE_POSITION
    }
  }

  override fun getItemId(position: Int): Long {
    return when (val state = getItem(position)) {
      is PositionItemViewState.Footer -> Long.MIN_VALUE
      is PositionItemViewState.Position -> state.id.hashCode().toLong()
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasePositionItemViewHolder<*> {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      TYPE_POSITION -> {
        val binding = PositionItemHolderBinding.inflate(inflater, parent, false)
        PositionItemViewHolder(binding, factory, owner, callback)
      }
      TYPE_FOOTER -> {
        val binding = ListitemFrameBinding.inflate(inflater, parent, false)
        PositionFooterViewHolder(binding.listitemFrame, factory, owner)
      }
      else -> throw AssertionError("View Type must be one of the supported. $viewType")
    }
  }

  override fun onBindViewHolder(holder: BasePositionItemViewHolder<*>, position: Int) {
    return when (val state = getItem(position)) {
      is PositionItemViewState.Footer -> {
        val viewHolder = holder as PositionFooterViewHolder
        viewHolder.bindState(state)
      }
      is PositionItemViewState.Position -> {
        val viewHolder = holder as PositionItemViewHolder
        viewHolder.bindState(state)
      }
    }
  }

  interface Callback {

    fun onRemove(index: Int)
  }
}
