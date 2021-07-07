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

package com.pyamsoft.tickertape.home

import android.view.ViewGroup
import androidx.core.view.updatePadding
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.home.databinding.HomeContainerBinding
import javax.inject.Inject

class HomeContainer @Inject internal constructor(parent: ViewGroup) :
    BaseUiView<HomeViewState, HomeViewEvent, HomeContainerBinding>(parent) {

  override val layoutRoot by boundView { homeContainer }

  override val viewBinding = HomeContainerBinding::inflate

  override fun onRender(state: UiRender<HomeViewState>) {
    state.mapChanged { it.bottomOffset }.render(viewScope) { handleBottomOffset(it) }
  }

  private fun handleBottomOffset(height: Int) {
    // Multiply by 2 to account for the bar offset and the height change in MainContainer
    layoutRoot.updatePadding(bottom = height * 2)
  }
}
