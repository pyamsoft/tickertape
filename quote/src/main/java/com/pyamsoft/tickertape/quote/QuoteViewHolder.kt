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

import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.ui.databinding.ListitemFrameBinding
import javax.inject.Inject

class QuoteViewHolder
internal constructor(binding: ListitemFrameBinding, factory: QuoteComponent.Factory) :
    RecyclerView.ViewHolder(binding.root), ViewBinder<QuoteViewState> {

  @Inject @JvmField internal var quote: QuoteView? = null

  private val viewBinder: ViewBinder<QuoteViewState>

  init {
    factory.create(binding.listitemFrame).inject(this)

    val quote = requireNotNull(quote)

    viewBinder = createViewBinder(quote) {}
  }

  override fun bindState(state: QuoteViewState) {
    viewBinder.bindState(state)
  }

  override fun teardown() {
    viewBinder.teardown()

    quote = null
  }
}
