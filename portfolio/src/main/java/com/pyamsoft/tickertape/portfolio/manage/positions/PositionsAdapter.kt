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
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemComponent
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemViewHolder
import com.pyamsoft.tickertape.portfolio.manage.positions.item.PositionItemViewState
import me.zhanghai.android.fastscroll.PopupTextProvider

class PositionsAdapter
private constructor(
    private val factory: PositionItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<PositionItemViewState, PositionItemViewHolder>(DIFFER), PopupTextProvider {

  init {
    setHasStableIds(true)
  }

  companion object {

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
            return oldItem.holding.id() == newItem.holding.id()
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
    val state = getItem(position)
    return state.holding.symbol().symbol()
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).holding.id().hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionItemViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ListitemFrameBinding.inflate(inflater, parent, false)
    return PositionItemViewHolder(binding, factory, owner, callback)
  }

  override fun onBindViewHolder(holder: PositionItemViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }

  interface Callback {

    fun onRemove(index: Int)
  }
}
