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

package com.pyamsoft.tickertape.quote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

class QuoteAdapter
private constructor(
    private val factory: QuoteComponent.Factory,
    private val owner: LifecycleOwner,
    private val callback: Callback
) : ListAdapter<QuoteViewState, QuoteViewHolder>(DIFFER), PopupTextProvider {

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        factory: QuoteComponent.Factory,
        owner: LifecycleOwner,
        callback: Callback
    ): QuoteAdapter {
      return QuoteAdapter(factory, owner, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<QuoteViewState>() {
          override fun areItemsTheSame(oldItem: QuoteViewState, newItem: QuoteViewState): Boolean {
            return oldItem.symbol.symbol() == newItem.symbol.symbol()
          }

          override fun areContentsTheSame(
              oldItem: QuoteViewState,
              newItem: QuoteViewState
          ): Boolean {
            return oldItem == newItem
          }
        }
  }

  override fun getPopupText(position: Int): String {
    val state = getItem(position)
    return state.symbol.symbol()
  }

  override fun getItemId(position: Int): Long {
    return getItem(position).symbol.symbol().hashCode().toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ListitemFrameBinding.inflate(inflater, parent, false)
    return QuoteViewHolder(binding, factory, owner, callback)
  }

  override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
    val state = getItem(position)
    holder.bindState(state)
  }

  interface Callback {

    fun onRemove(index: Int)
  }
}
