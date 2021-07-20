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

package com.pyamsoft.tickertape.portfolio.manage.positions.item

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.util.doOnDestroy

abstract class BasePositionItemViewHolder<S : PositionItemViewState>
protected constructor(
    itemView: View,
    owner: LifecycleOwner,
) : RecyclerView.ViewHolder(itemView), ViewBinder<S> {

  protected abstract val viewBinder: ViewBinder<S>

  init {
    owner.doOnDestroy { teardown() }
  }

  final override fun bindState(state: S) {
    viewBinder.bindState(state)
  }

  final override fun teardown() {
    viewBinder.teardown()
    onTeardown()
  }

  protected abstract fun onTeardown()
}
