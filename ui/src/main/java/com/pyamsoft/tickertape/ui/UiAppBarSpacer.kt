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

package com.pyamsoft.tickertape.ui

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.ui.app.AppBarActivity
import com.pyamsoft.pydroid.ui.util.applyAppBarOffset
import com.pyamsoft.tickertape.ui.databinding.UiAppBarSpacerBinding

abstract class UiAppBarSpacer<S : UiViewState, V : UiViewEvent>
protected constructor(
    parent: ViewGroup,
    owner: LifecycleOwner,
    appBarActivity: AppBarActivity,
) : BaseUiView<S, V, UiAppBarSpacerBinding>(parent) {

  final override val viewBinding = UiAppBarSpacerBinding::inflate

  final override val layoutRoot by boundView { uiSpacerAppbar }

  init {
    doOnInflate {
      // Remove outline provider to stop shadow
      binding.uiSpacerAppbar.apply {
        outlineProvider = null
        elevation = 0F
      }
    }

    doOnInflate {
      binding.uiSpacerCollapse.apply { post { applyAppBarOffset(appBarActivity, owner) } }
    }
  }

  final override fun onRender(state: UiRender<S>) {
    super.onRender(state)
    // Don't let subclasses override this method
  }
}
