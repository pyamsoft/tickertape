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

package com.pyamsoft.tickertape.main

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.tickertape.ui.UiFragmentContainer
import javax.inject.Inject

class MainContainer @Inject internal constructor(parent: ViewGroup) :
    UiFragmentContainer<MainViewState, MainViewEvent>(parent) {

  private var initialHeight = 0
  private var lastBottomBarHeight = 0

  init {
    doOnInflate {
      layoutRoot.also { v ->
        v.post {
          initialHeight = v.height
          updateHeight()
        }
      }
    }
  }

  override fun onRender(state: UiRender<MainViewState>) {
    state.mapChanged { it.bottomBarHeight }.render(viewScope) { handleBottomBarHeight(it) }
  }

  private fun handleBottomBarHeight(height: Int) {
    lastBottomBarHeight = height
    updateHeight()
  }

  private fun updateHeight() {
    // Add additional height to the main container so that when it scrolls as a result of the
    // coordinator layout,
    // we avoid the blank strip on the bottom.
    layoutRoot.updateLayoutParams { this.height = initialHeight + lastBottomBarHeight }
  }
}
