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

package com.pyamsoft.tickertape.quote.ui.component.chart.range

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.util.doOnDestroy
import com.pyamsoft.tickertape.quote.ui.databinding.ComponentChartRangeItemBinding
import javax.inject.Inject

class ChartRangeViewHolder
internal constructor(
    binding: ComponentChartRangeItemBinding,
    factory: ChartRangeComponent.Factory,
    owner: LifecycleOwner,
    callback: ChartRangeAdapter.Callback
) : RecyclerView.ViewHolder(binding.root), ViewBinder<ChartRangeViewState> {

  @Inject @JvmField internal var click: ChartRangeClick? = null

  @Inject @JvmField internal var text: ChartRangeText? = null

  private val viewBinder: ViewBinder<ChartRangeViewState>

  init {
    factory.create(binding.componentChartRangeItem).inject(this)

    viewBinder =
        createViewBinder(text.requireNotNull(), click.requireNotNull()) {
          return@createViewBinder when (it) {
            is ChartRangeViewEvent.Select -> callback.onSelect(bindingAdapterPosition)
          }
        }

    owner.doOnDestroy { teardown() }
  }

  override fun bindState(state: ChartRangeViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()
    text = null
    click = null
  }
}
