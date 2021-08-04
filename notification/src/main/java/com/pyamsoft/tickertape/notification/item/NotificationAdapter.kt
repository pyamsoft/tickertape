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

package com.pyamsoft.tickertape.notification.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.tickertape.notification.databinding.NotificationItemWrapperBinding
import com.pyamsoft.tickertape.notification.item.mover.BigMoverViewHolder
import com.pyamsoft.tickertape.notification.item.tape.TapeViewHolder
import com.pyamsoft.tickertape.ui.SpacerViewHolder
import kotlin.random.Random
import timber.log.Timber

internal class NotificationAdapter
private constructor(
    private val factory: NotificationItemComponent.Factory,
    private val owner: LifecycleOwner,
    private val appBarActivity: AppBarActivity,
    private val callback: Callback,
) : ListAdapter<NotificationItemViewState, RecyclerView.ViewHolder>(DIFFER) {

  companion object {

    private val SPACER_HASH_CODE = Random.nextInt()
    private const val VIEW_TYPE_SPACER = 1
    private const val VIEW_TYPE_BIG_MOVER = 2
    private const val VIEW_TYPE_TAPE = 3

    @JvmStatic
    @CheckResult
    fun create(
        factory: NotificationItemComponent.Factory,
        owner: LifecycleOwner,
        appBarActivity: AppBarActivity,
        callback: Callback
    ): NotificationAdapter {
      return NotificationAdapter(factory, owner, appBarActivity, callback)
    }

    private val DIFFER =
        object : DiffUtil.ItemCallback<NotificationItemViewState>() {

          override fun areItemsTheSame(
              oldItem: NotificationItemViewState,
              newItem: NotificationItemViewState
          ): Boolean {
            return when (oldItem) {
              is NotificationItemViewState.Tape -> newItem is NotificationItemViewState.Tape
              is NotificationItemViewState.BigMover -> newItem is NotificationItemViewState.BigMover
              is NotificationItemViewState.Spacer -> newItem is NotificationItemViewState.Spacer
            }
          }

          override fun areContentsTheSame(
              oldItem: NotificationItemViewState,
              newItem: NotificationItemViewState
          ): Boolean {
            return when (oldItem) {
              is NotificationItemViewState.Tape ->
                  when (newItem) {
                    is NotificationItemViewState.Tape -> oldItem == newItem
                    else -> false
                  }
              is NotificationItemViewState.BigMover ->
                  when (newItem) {
                    is NotificationItemViewState.BigMover -> oldItem == newItem
                    else -> false
                  }
              is NotificationItemViewState.Spacer -> newItem is NotificationItemViewState.Spacer
            }
          }
        }
  }

  override fun getItemId(position: Int): Long {
    return when (val state = getItem(position)) {
      is NotificationItemViewState.BigMover -> state.hashCode()
      is NotificationItemViewState.Tape -> state.hashCode()
      is NotificationItemViewState.Spacer -> SPACER_HASH_CODE
    }.toLong()
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is NotificationItemViewState.BigMover -> VIEW_TYPE_BIG_MOVER
      is NotificationItemViewState.Tape -> VIEW_TYPE_TAPE
      is NotificationItemViewState.Spacer -> VIEW_TYPE_SPACER
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      VIEW_TYPE_TAPE -> {
        val binding = NotificationItemWrapperBinding.inflate(inflater, parent, false)
        TapeViewHolder(binding, factory, owner, callback)
      }
      VIEW_TYPE_BIG_MOVER -> {
        val binding = NotificationItemWrapperBinding.inflate(inflater, parent, false)
        BigMoverViewHolder(binding, factory, owner, callback)
      }
      VIEW_TYPE_SPACER -> SpacerViewHolder.create(parent, appBarActivity, owner)
      else -> throw AssertionError("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    return when (val state = getItem(position)) {
      is NotificationItemViewState.Tape -> {
        val viewHolder = holder as TapeViewHolder
        viewHolder.bindState(state)
      }
      is NotificationItemViewState.BigMover -> {
        val viewHolder = holder as BigMoverViewHolder
        viewHolder.bindState(state)
      }
      is NotificationItemViewState.Spacer -> Timber.d("Ignore bind on Spacer item")
    }
  }

  interface Callback : TapeCallback, BigMoverCallback

  interface TapeCallback {

    fun onTapeUpdated(enabled: Boolean)
  }

  interface BigMoverCallback {

    fun onBigMoverUpdated(enabled: Boolean)
  }
}
