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
import androidx.core.view.updateLayoutParams
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.ui.databinding.BottomSpacerBinding

abstract class UiBottomSpacer<S : UiViewState, V : UiViewEvent>
protected constructor(parent: ViewGroup) : BaseUiView<S, V, BottomSpacerBinding>(parent) {

  final override val layoutRoot by boundView { bottomSpacer }

  final override val viewBinding = BottomSpacerBinding::inflate

  protected fun handleBottomOffset(height: Int) {
    layoutRoot.updateLayoutParams { this.height = height }
  }
}