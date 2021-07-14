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

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.ImageTarget
import com.pyamsoft.pydroid.ui.R
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.util.tintWith
import com.pyamsoft.tickertape.ui.databinding.UiToolbarBinding

abstract class UiDialogToolbar<S : UiViewState, V : UiViewEvent>
protected constructor(
    imageLoader: ImageLoader,
    parent: ViewGroup,
) : BaseUiView<S, V, UiToolbarBinding>(parent) {

  final override val viewBinding = UiToolbarBinding::inflate

  final override val layoutRoot by boundView { uiAppbar }

  init {
    doOnInflate {
      imageLoader
          .asDrawable()
          .load(R.drawable.ic_close_24dp)
          .mutate { it.tintWith(Color.WHITE) }
          .into(
              object : ImageTarget<Drawable> {

                override fun clear() {
                  binding.uiToolbar.navigationIcon = null
                }

                override fun setImage(image: Drawable) {
                  binding.uiToolbar.setUpEnabled(true, image)
                }
              })
          .also { loaded -> doOnTeardown { loaded.dispose() } }
    }

    doOnInflate { binding.uiAppbar.withRoundedBackground() }
  }
}
