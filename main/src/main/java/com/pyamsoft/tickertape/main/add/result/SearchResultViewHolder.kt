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

package com.pyamsoft.tickertape.main.add.result

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.util.doOnDestroy
import javax.inject.Inject

class SearchResultViewHolder
internal constructor(
    itemView: ViewGroup,
    factory: SearchResultComponent.Factory,
    owner: LifecycleOwner,
    callback: SearchResultAdapter.Callback,
) : RecyclerView.ViewHolder(itemView), ViewBinder<SearchResultViewState> {

  private val viewBinder: ViewBinder<SearchResultViewState>

  @Inject @JvmField internal var name: SearchResultName? = null

  @Inject @JvmField internal var click: SearchResultClick? = null

  init {
    factory.create(itemView).inject(this)

    viewBinder =
        createViewBinder(name.requireNotNull(), click.requireNotNull()) {
          return@createViewBinder when (it) {
            is SearchResultViewEvent.Select -> callback.onSelect(bindingAdapterPosition)
          }
        }

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: SearchResultViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()
  }
}
