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

package com.pyamsoft.tickertape.home.index

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.home.databinding.HomeIndexItemBinding
import javax.inject.Inject

class HomeIndexViewHolder
internal constructor(
    binding: HomeIndexItemBinding,
    factory: HomeIndexComponent.Factory,
    owner: LifecycleOwner,
    callback: HomeIndexAdapter.Callback,
) : RecyclerView.ViewHolder(binding.root), ViewBinder<HomeIndexViewState> {

  @Inject @JvmField internal var symbol: HomeIndexSymbol? = null

  @Inject @JvmField internal var chart: HomeIndexChart? = null

  @Inject @JvmField internal var click: HomeIndexClick? = null

  private val viewBinder: ViewBinder<HomeIndexViewState>

  init {
    factory.create(binding.homeIndexItem).inject(this)

    viewBinder =
        createViewBinder(
            symbol.requireNotNull(),
            chart.requireNotNull(),
            click.requireNotNull(),
        ) {
          return@createViewBinder when (it) {
            is HomeIndexViewEvent.DigDeeper -> callback.onSelected(bindingAdapterPosition)
          }
        }

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: HomeIndexViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    symbol = null
    chart = null
  }
}
