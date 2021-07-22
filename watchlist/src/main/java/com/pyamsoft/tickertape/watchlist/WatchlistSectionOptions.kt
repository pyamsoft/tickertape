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

package com.pyamsoft.tickertape.watchlist

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.ui.UiSectionOptions
import javax.inject.Inject

class WatchlistSectionOptions @Inject internal constructor(parent: ViewGroup) :
    UiSectionOptions<WatchListViewState, WatchListViewEvent>(parent) {

  init {
    // For some reason the match_parent height does not make this list fill content
    // Grab the size of the activity parent and use it as our height
    doOnInflate { parent.post { layoutRoot.updateLayoutParams { this.height = parent.height } } }
  }

  override fun onRender(state: UiRender<WatchListViewState>) {
    state.mapChanged { it.section }.render(viewScope) { handleSection(it) }
  }
}
