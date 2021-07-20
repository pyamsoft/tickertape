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

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.ViewBinder
import com.pyamsoft.pydroid.arch.createViewBinder
import com.pyamsoft.pydroid.core.requireNotNull
import javax.inject.Inject

class PositionFooterViewHolder
internal constructor(
    itemView: ViewGroup,
    factory: PositionItemComponent.Factory,
    owner: LifecycleOwner,
) : BasePositionItemViewHolder<PositionItemViewState.Footer>(itemView, owner) {

  override val viewBinder: ViewBinder<PositionItemViewState.Footer>

  @Inject @JvmField internal var footer: PositionFooterView? = null

  init {
    factory.create(itemView).inject(this)

    viewBinder = createViewBinder(footer.requireNotNull()) {}
  }

  override fun onTeardown() {}
}
