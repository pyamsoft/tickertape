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
import android.widget.LinearLayout
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.tickertape.ui.databinding.ContainerVerticalBinding

abstract class UiVerticalContainer<S : UiViewState, V : UiViewEvent>
protected constructor(parent: ViewGroup) : BaseUiView<S, V, ContainerVerticalBinding>(parent) {

  final override val layoutRoot by boundView { containerVertical }

  final override val viewBinding = ContainerVerticalBinding::inflate

  final override fun adoptionParent(): LinearLayout {
    return binding.containerVertical
  }
}
