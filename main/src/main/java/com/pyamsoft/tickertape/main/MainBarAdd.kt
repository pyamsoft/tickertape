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
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UiRender
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.tickertape.main.databinding.MainAddBinding
import com.pyamsoft.tickertape.ui.R
import javax.inject.Inject

class MainBarAdd
@Inject
internal constructor(private val imageLoader: ImageLoader, parent: ViewGroup) :
    BaseUiView<MainViewState, MainViewEvent, MainAddBinding>(parent) {

  override val viewBinding = MainAddBinding::inflate

  override val layoutRoot by boundView { mainBarAdd }

  init {
    doOnInflate {
      binding.mainBarAdd.setOnDebouncedClickListener { publish(MainViewEvent.AddRequest) }
    }

    doOnInflate {
      imageLoader.asDrawable().load(R.drawable.ic_add_24dp).into(binding.mainBarAdd).also { l ->
        doOnTeardown { l.dispose() }
      }
    }

    doOnTeardown { binding.mainBarAdd.setOnDebouncedClickListener(null) }
  }

  override fun onRender(state: UiRender<MainViewState>) {
    state.mapChanged { it.isFabVisible }.render(viewScope) { handleFabVisible(it) }
  }

  private fun handleFabVisible(visible: Boolean) {
    binding.mainBarAdd.apply { if (visible) show() else hide() }
  }
}
